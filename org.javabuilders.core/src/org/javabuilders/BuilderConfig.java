/**
 * 
 */
package org.javabuilders;

import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.beanutils.PropertyUtils;
import org.javabuilders.event.BuildListener;
import org.javabuilders.event.IBackgroundProcessingHandler;
import org.javabuilders.handler.DefaultPropertyHandler;
import org.javabuilders.handler.DefaultTypeHandler;
import org.javabuilders.handler.IPropertyHandler;
import org.javabuilders.handler.ITypeHandler;
import org.javabuilders.handler.IntegerAsValueHandler;
import org.javabuilders.handler.binding.BuilderBindings;
import org.javabuilders.handler.type.IntArrayAsValueHandler;
import org.javabuilders.handler.type.IntegerArrayAsValueHandler;
import org.javabuilders.handler.validation.BuilderValidators;
import org.javabuilders.handler.validation.DefaultValidatorTypeHandler;
import org.javabuilders.handler.validation.IValidationMessageHandler;

/**
 * Represents the configuration for a builder (e.g. Swing vs SWT, etc)
 * @author Jacek Furmankiewicz
 */
public class BuilderConfig {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(BuilderConfig.class.getSimpleName());
	
	private static ITypeHandler defaultTypeHandler = new DefaultTypeHandler();
	private static IPropertyHandler defaultPropertyHandler =  DefaultPropertyHandler.getInstance();
	private static String devSourceFolder = null;
	public static final String SOURCE = "javabuilders.dev.src";
	public final static String CUSTOM_COMMAND_REGEX = "\\$[a-zA-Z0-9]+"; //e.g. "$validate"
	public final static String GLOBAL_VARIABLE_REGEX = "\\$\\$\\{[a-zA-Z0-9]+\\}"; //e.g. "$${dateFormat}"
	private static Set<ResourceBundle> bundles = new LinkedHashSet<ResourceBundle>();

	
	/**
	 * Static constructor
	 */
	static {
		devSourceFolder = System.getProperty(SOURCE);
	}
	
	/**
	 * @return Current dev source folder (usually null unless overwritted with -Djavabuilders.dev.src)
	 */
	public static String getDevSourceFolder() {
		return devSourceFolder;
	}

	/**
	 * @param devSourceFolder Development source folder. Allows to hot deploy YAML content without restarting the whole app
	 */
	public static void setDevSourceFolder(String devSourceFolder) {
		BuilderConfig.devSourceFolder = devSourceFolder;
	}
	
	private Map<Class<?>,Map<String,IPropertyHandler>>  propertyHandlers = new HashMap<Class<?>,Map<String,IPropertyHandler>>();
	
	protected Map<Class<?>,Map<String,IPropertyHandler>> getPropertyHandlers() {
		return propertyHandlers;
	}
	private Map<Class<?>,ITypeHandler> typeHandlers = new HashMap<Class<?>,ITypeHandler>();
	
	private Map<Class<?>,TypeDefinition> typeDefinitions = new HashMap<Class<?>, TypeDefinition>();
	private Map<String,Class<?>> typeAliases = new HashMap<String, Class<?>>();
	
	private Map<Class<?>,String> namedObjectCriteria = new HashMap<Class<?>,String>();
	
	private boolean markInvalidResourceBundleKeys = true;

	//internal cache used to avoid re-creating the hierarchy of type definitions with every request
	//loaded lazily, upon demand
	private Map<Class<?>,Set<TypeDefinition>> typeDefinitionsForClassCache = new HashMap<Class<?>, Set<TypeDefinition>>();
	
	private IBackgroundProcessingHandler backgroundProcessingHandler = null;
	private IValidationMessageHandler validationMessageHandler = null;
	
	private Map<String,ICustomCommand<? extends Object>> customCommands = new HashMap<String, ICustomCommand<? extends Object>>();
	
	private Set<BuildListener> buildListeners = new LinkedHashSet<BuildListener>();
	
	private Map<String,Object> customProperties = new HashMap<String, Object>();
	
	private Map<String,Object> globals = new HashMap<String, Object>();
	
	/**
	 * Constructor
	 * @param backgroundProcessingHandler Domain-specific background processing handler
	 */
	public BuilderConfig(IBackgroundProcessingHandler backgroundProcessingHandler, 
			ITypeHandler bindingTypeHandler,
			IValidationMessageHandler validationMessageHandler, ICustomCommand<Boolean> confirmCommand) {
		this.backgroundProcessingHandler = backgroundProcessingHandler;
		this.validationMessageHandler = validationMessageHandler;

		customCommands.put(Builder.CONFIRM_CUSTOM_COMMAND, confirmCommand);
		
		addType(Builder.BIND, BuilderBindings.class);
		addType(Builder.VALIDATE, BuilderValidators.class);

		if (bindingTypeHandler != null) {
			addTypeHandler(bindingTypeHandler);
		}
		addTypeHandler(DefaultValidatorTypeHandler.getInstance());
		
		//handler for static final int constants
		forType(int.class).valueHandler(IntegerAsValueHandler.getInstance());
		forType(int[].class).valueHandler(IntArrayAsValueHandler.getInstance());
		forType(Integer[].class).valueHandler(IntegerArrayAsValueHandler.getInstance());
		
		//default custom commands
		addCustomCommand(Builder.VALIDATE_CUSTOM_COMMAND, new ICustomCommand<Boolean>() {
			public Boolean process(BuildResult result, Object source) {
				return result.validate();
			}
		});
	}
	
	/**
	 * Registers a handler for one or more key/value pairs for a particular class type (including all of its descendants)
	 * @param handler Property handler
	 * @return Current instance, for use in builder pattern
	 */
	public BuilderConfig addPropertyHandler(IPropertyHandler handler) {
		if (handler == null) {
			throw new NullPointerException("handler cannot be null");
		}
		if (handler.getApplicableClass() == null) {
			throw new NullPointerException("IPropertyHandler.getApplicableClass() cannot be null");
		}
		//register the handler for each of the keys it is supposed to consume
		for(String key : handler.getConsumedKeys()) {
			Map<String,IPropertyHandler> handlers = propertyHandlers.get(handler.getApplicableClass());
			if (handlers == null) {
				handlers = new HashMap<String,IPropertyHandler>();
				propertyHandlers.put(handler.getApplicableClass(), handlers);
			}
			handlers.put(key, handler);
		}
		return this;
	}
	
	/**
	 * Adds a type handler for a particular class and all of its children
	 * @param typeHandler Type handler
	 * @return Current instance, for use in Builder pattern
	 */
	public BuilderConfig addTypeHandler(ITypeHandler typeHandler) {
		if (typeHandler == null) {
			throw new NullPointerException("typeHandler cannot be null");
		}		
		if (typeHandler.getApplicableClass() == null) {
			throw new NullPointerException("ITypeHandler.getApplicableClass() cannot be null");
		}		
		typeHandlers.put(typeHandler.getApplicableClass(),typeHandler);
		return this;
	}
	
	/**
	 * Defines metadata about a type, e.g.<br/>
	 * <code>
	 * defineType(JFrame.class).requires("name","text").requires(LayoutManager.class).delay(LayoutManager.class);
	 * @param applicableClass Class to which this type definition applies
	 * @return The created type definition so that it can be used via the Builder pattern
	 */
	public TypeDefinition forType(Class<?> applicableClass) {
		if (applicableClass == null) {
			throw new NullPointerException("applicableClass cannot be null");
		}
		TypeDefinition def = null;
		//lazy creation
		if (typeDefinitions.containsKey(applicableClass)) {
			def = typeDefinitions.get(applicableClass);
		} else {
			def = new TypeDefinition(applicableClass);
			typeDefinitions.put(applicableClass, def);
			
			//every time a new type definition is created
			//let's clear the cache to be on the safe side
			typeDefinitionsForClassCache.clear();
		}
		return def;
	}
	
	/**
	 * Adds multiple classes with aliases that correspond to the class names,, e.g.
	 * <code>
	 * addType(JFrame.class, JButton.class);
	 * <code>
	 * which corresponds to the type names in the builder file, e.g.
	 * <code>
	 * <b>JFrame</b>:
	 *     name: myFrame
	 *     title: My Frame
	 *     <b>JButton</b>:
	 *         name: myButton
	 *         text: My Button
	 * </code>
	 * @param classTypes Class types
	 * @return BuilderConfig (for use in Builder pattern) 
	 */
	public BuilderConfig addType(Class<?>... classTypes) {
		for(Class<?> type : classTypes) {
			addType(type);
		}
		return this;
	}
	
	/**
	 * Adds an alias and the class type that corresponds to it
	 * (assuming both names are the same), e.g.
	 * <code>
	 * addType(JFrame.class);
	 * addType(JButton.class);
	 * <code>
	 * which corresponds to the type names in the builder file, e.g.
	 * <code>
	 * <b>JFrame</b>:
	 *     name: myFrame
	 *     title: My Frame
	 *     <b>JButton</b>:
	 *         name: myButton
	 *         text: My Button
	 * </code>
	 * @param classType Class type
	 * @return BuilderConfig (for use in Builder pattern) 
	 */
	public BuilderConfig addType(Class<?> classType) {
		addType(classType.getSimpleName(),classType);
		return this;
	}
	
	/**
	 * Adds an alias and the class type that corresponds to it, e.g.
	 * <code>
	 * addType("JFrame",JFrame.class);
	 * addType("JButton",JButton.class);
	 * <code>
	 * which corresponds to the type names in the builder file, e.g.
	 * <code>
	 * <b>JFrame</b>:
	 *     name: myFrame
	 *     title: My Frame
	 *     <b>JButton</b>:
	 *         name: myButton
	 *         text: My Button
	 * </code>
	 * @param alias Alias
	 * @param classType Class type
	 * @return BuilderConfig (for use in Builder pattern) 
	 */
	public BuilderConfig addType(String alias, Class<?> classType) {
		if (alias == null || alias.length() == 0) {
			throw new NullPointerException("alias cannot be null or empty");
		}
		if (classType == null) {
			throw new NullPointerException("classType cannot be null");
		}
		if (typeAliases.containsKey(alias)) {
			String error = String.format("Duplicate alias '%s' for class '%s'. One already exists for '%s'",
					alias,classType.getName(),(typeAliases.get(alias)).getName());
			throw new DuplicateAliasException(error);
		}
		typeAliases.put(alias, classType);
		return this;
	}

	/**
	 * Gets all the defined type definitions, by class
	 * @return
	 */
	public Collection<TypeDefinition> getTypeDefinitions() {
		return typeDefinitions.values();
	}
	
	/**
	 * Returns the exact type definition for a particular class
	 * @param classType Class type
	 * @return Type definition or null if none found
	 */
	public TypeDefinition getTypeDefinition(Class<?> classType) {
		return typeDefinitions.get(classType);
	}
	
	/**
	 * Gets a list of all the pertinent type definitions.
	 * @param classType Class type
	 * @return Type definitions, sorted by inheritance tree
	 */
	public Set<TypeDefinition> getTypeDefinitions(Class<?> classType) {
		if (classType == null) {
			throw new NullPointerException("classType cannot be null");
		}
		
		Set<TypeDefinition> defs = null;
		if (typeDefinitionsForClassCache.containsKey(classType)) {
			defs = typeDefinitionsForClassCache.get(classType);
		} else {
			//first request - lazy creation
			defs = new TreeSet<TypeDefinition>(new TypeDefinitionClassHierarchyComparator());
			
			Class<?> superClass = classType;
			while (superClass != null) {
				if (typeDefinitions.containsKey(superClass)) {
					defs.add(typeDefinitions.get(superClass));
				}			
				superClass = superClass.getSuperclass();
			}
			typeDefinitionsForClassCache.put(classType, defs);
		}
		
		return defs;
	}
	
	/**
	 * Gets the type handler for a specific type alias
	 * @param classType Class type
	 * @return Type handler
	 */
	public ITypeHandler getTypeHandler(Class<?> classType) {

		if (classType == null) {
			throw new NullPointerException("classType cannot be null");
		}
		
		//this alias is a type, not just a property
		ITypeHandler typeHandler = defaultTypeHandler;
		Class<?> parentType = classType;
		
		//go from the bottom up the hierarchy tree...the lowest
		//type handler wins
		while (parentType != null) {
			if (typeHandlers.containsKey(parentType)) {
				typeHandler = typeHandlers.get(parentType);
				break;
			} 
			parentType = parentType.getSuperclass();
		}
		
		return typeHandler;
	}
	
	/**
	 * Returns the handler for a specific property
	 * @param classType The type of the object being processed
	 * @param key The name of the property
	 * @return The handler for that property (and potentially others, if it consumes multiple ones)
	 */
	public IPropertyHandler getPropertyHandler(Class<?> classType, String key) {
		IPropertyHandler handler = defaultPropertyHandler;
		
		Class<?> parentClass = classType;
		
		//find the first applicable property handler in the object hierarchy,
		//unless one is found that handles ALL properties
		while (parentClass != null) {
			Map<String,IPropertyHandler> handlers = getPropertyHandlers().get(parentClass);
			if (handlers != null && handlers.containsKey(key)) {
				handler = handlers.get(key);
				break;
			}
			parentClass = parentClass.getSuperclass();
		}
		
		return handler;
	}
	
	/**
	 * Indicates if a class is defined as a type
	 * @param classType Class type
	 * @return
	 */
	public boolean isTypeDefined(Class<?> classType) {
		return typeDefinitions.containsKey(classType);
	}
	
	/**
	 * Returns the class type associated with a particular alias. Null if none found.
	 * Should never be called directly, use BuilderUtils.getClassFromAlias() instead.
	 * @param alias Alias
	 * @return Class (null if none found)
	 */
	Class<?> getClassType(String alias)  {
		Class<?> classType = typeAliases.get(alias);
		return classType;
	}
	
	/**
	 * Indicates if an object instance should be treated as a unique
	 * named objects and added to the list of named objects in the
	 * BuildResult
	 * @param instance
	 * @return true if named, false if not
	 */
	public boolean isNamedObject(Object instance) {
		boolean isNamed = false;
		
		for(Class<?> classType : namedObjectCriteria.keySet()) {
			if (classType.isInstance(instance)) {
				isNamed = true;
				break;
			}
		}
		
		return isNamed;
	}
	
	/**
	 * Checks the raw data (before an object has been even handled) to extract its name, 
	 * if it has been specified
	 * @param currentType Class type
	 * @param data Raw parses data
	 * @return Name or null if not found
	 */
	public String getNameIfAvailable(Class<?> currentType, Map<String,Object> data) {
		String name = null;
		
		for(Class<?> classType : namedObjectCriteria.keySet()) {
			if (classType.isAssignableFrom(currentType)) {
				String propertyName = namedObjectCriteria.get(classType);
				name = (String) data.get(propertyName);
				break;
			}
		}
		
		return name;
	}
	
	/**
	 * Returns the name of an object instance
	 * @param instance
	 * @return Object name
	 * @throws ConfigurationException 
	 */
	public String getObjectName(Object instance) throws ConfigurationException {
		String name = null;
		for(Class<?> classType : namedObjectCriteria.keySet()) {
			if (classType.isInstance(instance)) {
				String nameProperty = namedObjectCriteria.get(classType);
				try {
					//Issue #20 - fix for null names
					Object value = PropertyUtils.getProperty(instance,nameProperty);
					if (value == null) {
						name = null;
					} else {
						name = String.valueOf(value);
					}
					
				} catch (Exception e) {
					throw new ConfigurationException("Invalid named objects configuration",e);
				}
			}
		}
		return name;
	}
	
	/**
	 * Specifies that any object of this type with this property should be
	 * added to the list of named objects in the BuildResult, using the
	 * value of the specified property as the name
	 * @param classType Class type
	 * @param nameProperty Property name
	 */
	public void addNamedObjectCriteria(Class<?> classType, String nameProperty) {
		namedObjectCriteria.put(classType, nameProperty);
	}

	/**
	 * Returns the flag that controls if resource keys in the builder file
	 * are surrounded with "#" if not found in the list of ResourceBundles
	 * @return The markInvalidResourceBundleKeys flag
	 */
	public boolean isMarkInvalidResourceBundleKeys() {
		return markInvalidResourceBundleKeys;
	}

	/**
	 * Sets the flag that controls if resource keys in the builder file
	 * are surrounded with "#" if not found in the list of ResourceBundles
	 * @param markInvalidResourceBundleKeys The markInvalidResourceBundleKeys flag
	 */
	public void setMarkInvalidResourceBundleKeys(
			boolean markInvalidResourceBundleKeys) {
		this.markInvalidResourceBundleKeys = markInvalidResourceBundleKeys;
	}

	/**
	 * @return The domain-specific background processing handler
	 */
	public IBackgroundProcessingHandler getBackgroundProcessingHandler() {
		return backgroundProcessingHandler;
	}

	/**
	 * @param backgroundProcessingHandler The domain-specific background processing handler
	 */
	public void setBackgroundProcessingHandler(
			IBackgroundProcessingHandler backgroundProcessingHandler) {
		this.backgroundProcessingHandler = backgroundProcessingHandler;
	}

	/**
	 * @return the domain-specific validation message handler
	 */
	public IValidationMessageHandler getValidationMessageHandler() {
		return validationMessageHandler;
	}

	/**
	 * @param validationMessageHandler the domain-specific validation message handler to set
	 */
	public void setValidationMessageHandler(
			IValidationMessageHandler validationMessageHandler) {
		this.validationMessageHandler = validationMessageHandler;
	}
	
	/**
	 * Allows adding of custom commands
	 * @param globalName Global name
	 * @param command Command
	 * @return Current config
	 */
	public BuilderConfig addCustomCommand(String globalName, ICustomCommand<Boolean> command)  {
		BuilderUtils.validateNotNullAndNotEmpty("globalName", globalName);
		BuilderUtils.validateNotNullAndNotEmpty("command", command);
		
		if (globalName.matches(CUSTOM_COMMAND_REGEX)) {

			if (customCommands.containsKey(globalName)) {
				throw new BuildException("A custom command with the global name " + globalName + " is already defined"); 
			} else {
					customCommands.put(globalName, command);
			}

		} else {
			throw new BuildException(globalName + " is not a valid custom command name. Must start with '$'");
		}
		
		return this;
	}
	
	/**
	 * @return Custom commands
	 */
	Map<String,ICustomCommand<? extends Object>> getCustomCommands() {
		return customCommands;
	}

	/**
	 * @return Global resource bundles
	 */
	public Set<ResourceBundle> getResourceBundles() {
		return bundles;
	}
	
	/**
	 * Gets string resource from the specified bundles
	 * @param key Key
	 * @return String (or null if none found)
	 */
	public String getResource(String key) {
		String value = null;
		for(ResourceBundle  bundle : getResourceBundles()) {
			if (bundle.containsKey(key)) {
				value = bundle.getString(key);
				break;
			}
		}
		return value;
	}
	
	/**
	 * Add a global resource bundle
	 * @param resourceBundleName Bundle name
	 */
	public void addResourceBundle(String resourceBundleName) {
		getResourceBundles().add(ResourceBundle.getBundle(resourceBundleName));
	}

	/**
	 * Add a global resource bundle
	 * @param resourceBundle Bundle 
	 */
	public void addResourceBundle(ResourceBundle resourceBundle) {
		getResourceBundles().add(resourceBundle);
	}
	
	/**
	 * Adds a build listener
	 * @param listener Build listener
	 */
	public void addBuildListener(BuildListener listener) {
		buildListeners.add(listener);
	}
	
	/**
	 * Removes a build listener
	 * @param listener Build listener
	 */
	public void removeBuildListener(BuildListener listener) {
		if (buildListeners.contains(listener)) {
			buildListeners.remove(listener);
		}
	}
	
	/**
	 * @return Build listeners
	 */
	public BuildListener[] getBuildListeners() {
		return buildListeners.toArray(new BuildListener[0]);
	}

	/**
	 * Gets the collection of custom properties that allow to store any additional domain-specific settings
	 * @return the customProperties
	 */
	public Map<String, Object> getCustomProperties() {
		return customProperties;
	}
	
	/**
	 * Factory method that should be overriden for each toolkit with the 
	 * property change support that is proper for that toolkit's threading rules
	 * @return Domain-specific property change support
	 */
	public PropertyChangeSupport createPropertyChangeSupport(Object source) {
		return new PropertyChangeSupport(source);
	}
	

	/**
	 * Adds a global variable
	 * @param name Name
	 * @param value Value
	 * @return This
	 */
	public BuilderConfig addGlobalVariable(String name, Object value) {
		BuilderUtils.validateNotNullAndNotEmpty("name", name);
		BuilderUtils.validateNotNullAndNotEmpty("value", value);
		
		if (name.matches(GLOBAL_VARIABLE_REGEX)) {

			if (globals.containsKey(globals)) {
				throw new BuildException("A global variable {0} already exists", name); 
			} else {
				globals.put(name, value);
			}

		} else {
			throw new BuildException("{0} is not a valid global variable. Must start with '$'", name);
		}
		
		return this;
	}
	
	/**
	 * Gets global variable value
	 * @param name Name
	 * @param expectedType Expected variable type
	 * @return Value
	 */
	public Object getGlobalVariable(String name, Class<?> expectedType)  {
		BuilderUtils.validateNotNullAndNotEmpty("name", name);
		BuilderUtils.validateNotNullAndNotEmpty("expectedType", expectedType);
		
		Object value = globals.get(name);
		if (value == null) {
			throw new BuildException("Global variable {0} is null",name);
		}
		if (!expectedType.isAssignableFrom(value.getClass())) {
			throw new BuildException("Global variable {0} is not compatible with expected type {1}",
					name, expectedType);
		}
		
		return value;
	}

}
