/**
 * 
 */
package org.javabuilders.swt.handler.type.layout;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.javabuilders.BuildException;
import org.javabuilders.BuildProcess;
import org.javabuilders.BuilderConfig;
import org.javabuilders.InvalidPropertyFormatException;
import org.javabuilders.Node;
import org.javabuilders.handler.AbstractTypeHandler;
import org.javabuilders.handler.ITypeChildrenHandler;
import org.javabuilders.swt.SWTBuilder;

/**
 * FillLayout handler
 * @author Jacek Furmankiewicz
 *
 */
public class FillLayoutHandler extends AbstractTypeHandler implements ITypeChildrenHandler {

	private static final FillLayoutHandler singleton = new FillLayoutHandler();
	
	private Map<String,Integer> styleMap = new HashMap<String,Integer>();
	
	/**
	 * Returns singleton
	 * @return Singleton
	 */
	public static FillLayoutHandler getInstance() {
		return singleton;
	}
	
	/**
	 * Constructor
	 */
	public FillLayoutHandler() {
		super(SWTBuilder.MARGIN_HEIGHT,SWTBuilder.MARGIN_WIDTH,SWTBuilder.SPACING,SWTBuilder.STYLE);
		
		styleMap.put("horizontal", SWT.HORIZONTAL);
		styleMap.put("vertical", SWT.VERTICAL);
	}


	/* (non-Javadoc)
	 * @see org.javabuilders.handler.ITypeHandler#createNewInstance(org.javabuilders.BuilderConfig, org.javabuilders.BuildProcess, org.javabuilders.Node, java.lang.String, java.util.Map)
	 */
	public Node createNewInstance(BuilderConfig config, BuildProcess process,
			Node parent, String key, Map<String, Object> typeDefinition)
			throws BuildException {
		
		FillLayout instance = new FillLayout();
		return useExistingInstance(config, process, parent, key, typeDefinition, instance);
	}

	/* (non-Javadoc)
	 * @see org.javabuilders.handler.ITypeHandler#useExistingInstance(org.javabuilders.BuilderConfig, org.javabuilders.BuildProcess, org.javabuilders.Node, java.lang.String, java.util.Map, java.lang.Object)
	 */
	public Node useExistingInstance(BuilderConfig config, BuildProcess process,
			Node parent, String key, Map<String, Object> typeDefinition,
			Object instance) throws BuildException {
		Node node = new Node(parent,key,typeDefinition);
		node.setMainObject(instance);
		FillLayout layout = (FillLayout)instance;
		
		//handle all 4 potential properties
		if (typeDefinition.containsKey(SWTBuilder.MARGIN_HEIGHT)) {
			String value = String.valueOf(typeDefinition.get(SWTBuilder.MARGIN_HEIGHT));
			try {
				int intValue = Integer.parseInt(value);
				layout.marginHeight = intValue;
			} catch (NumberFormatException ex) {
				throw new InvalidPropertyFormatException(key,SWTBuilder.MARGIN_HEIGHT,value,SWTBuilder.MARGIN_HEIGHT + ": int",SWTBuilder.MARGIN_HEIGHT + ": 8",ex);
			}
		}
		
		if (typeDefinition.containsKey(SWTBuilder.MARGIN_WIDTH)) {
			String value = String.valueOf(typeDefinition.get(SWTBuilder.MARGIN_WIDTH));
			try {
				int intValue = Integer.parseInt(value);
				layout.marginWidth = intValue;
			} catch (NumberFormatException ex) {
				throw new InvalidPropertyFormatException(key,SWTBuilder.MARGIN_WIDTH,value,SWTBuilder.MARGIN_WIDTH + ": int",SWTBuilder.MARGIN_WIDTH + ": 8",ex);
			}
		}
		
		if (typeDefinition.containsKey(SWTBuilder.SPACING)) {
			String value = String.valueOf(typeDefinition.get(SWTBuilder.SPACING));
			try {
				int intValue = Integer.parseInt(value);
				layout.spacing = intValue;
			} catch (NumberFormatException ex) {
				throw new InvalidPropertyFormatException(key,SWTBuilder.SPACING,value,SWTBuilder.SPACING + ": int",SWTBuilder.SPACING + ": 8",ex);
			}
		}
		
		if (typeDefinition.containsKey(SWTBuilder.STYLE)) {
			String value = String.valueOf(typeDefinition.get(SWTBuilder.STYLE)).toLowerCase();
			
			if (styleMap.containsKey(value)) {
				Integer type = styleMap.get(value);
				layout.type = type;
			} else {
				throw new InvalidPropertyFormatException(key,SWTBuilder.STYLE,value,SWTBuilder.STYLE + ": horizontal|vertical",SWTBuilder.STYLE + ": vertical");
			}
		}
		
		
		Composite composite = (Composite)parent.getMainObject();
		composite.setLayout(layout);
		
		return node;
	}

	/* (non-Javadoc)
	 * @see org.javabuilders.IKeyValueConsumer#getApplicableClass()
	 */
	public Class<FillLayout> getApplicableClass() {
		return FillLayout.class;
	}

}
