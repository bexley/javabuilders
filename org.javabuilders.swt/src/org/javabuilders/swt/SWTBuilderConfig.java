/**
 * 
 */
package org.javabuilders.swt;

import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;

import net.miginfocom.swt.MigLayout;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.javabuilders.BuildResult;
import org.javabuilders.Builder;
import org.javabuilders.BuilderConfig;
import org.javabuilders.ICustomCommand;
import org.javabuilders.layout.DefaultResize;
import org.javabuilders.swt.handler.DefaultValidationMessageHandler;
import org.javabuilders.swt.handler.binding.JFaceDatabindingHandler;
import org.javabuilders.swt.handler.event.ButtonSelectionListenerHandler;
import org.javabuilders.swt.handler.event.MenuItemSelectionListenerHandler;
import org.javabuilders.swt.handler.event.background.SWTBackgroundProcessingHandler;
import org.javabuilders.swt.handler.property.LayoutNameHandler;
import org.javabuilders.swt.handler.property.SashBoundsHandler;
import org.javabuilders.swt.handler.property.WidgetNameHandler;
import org.javabuilders.swt.handler.type.DialogHandler;
import org.javabuilders.swt.handler.type.ImageAsValueHandler;
import org.javabuilders.swt.handler.type.MenuItemTypeHandler;
import org.javabuilders.swt.handler.type.ShellHandler;
import org.javabuilders.swt.handler.type.WidgetTypeHandler;
import org.javabuilders.swt.handler.type.layout.FillLayoutHandler;
import org.javabuilders.swt.handler.type.layout.MigSWTLayoutHandler;
import org.javabuilders.swt.handler.type.layout.StackLayoutHandler;

/**
 * SWT builder config
 * @author Jacek Furmankiewicz
 */
public class SWTBuilderConfig extends BuilderConfig {

	/**
	 * Constructor 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	public SWTBuilderConfig() {
		super(SWTBackgroundProcessingHandler.getInstance(),
				JFaceDatabindingHandler.getInstance(), 
				DefaultValidationMessageHandler.getInstance(),
				new ConfirmCommand());
		
		addType(
				Button.class,
				Browser.class,
				Canvas.class,
				CBanner.class,
				CCombo.class,
				CLabel.class,
				Combo.class,
				Composite.class,
				CoolBar.class,
				CoolItem.class,
				CTabFolder.class,
				DateTime.class,
				Dialog.class,
				ExpandBar.class,
				Group.class,
				Label.class,
				Link.class,
				List.class,
				Menu.class,
				MenuItem.class,
				ProgressBar.class,
				ProgressIndicator.class,
				Sash.class,
				SashForm.class,
				Scale.class,
				ScrollBar.class,
				ScrolledComposite.class,
				Shell.class,
				Slider.class,
				Spinner.class,
				StyledText.class,
				TabFolder.class,
				TabItem.class,
				Table.class,
				TableColumn.class,
				TableItem.class,
				TableCursor.class,
				Text.class,
				ToolBar.class,
				ToolItem.class,
				Tracker.class,
				Tray.class,
				TrayItem.class,
				Tree.class,
				TreeItem.class,
				ViewForm.class
			);
		
		addType(
				MigLayout.class,
				FillLayout.class,
				FormLayout.class,
				GridLayout.class,
				RowLayout.class,
				StackLayout.class
				);

        //define type-specific metadata
		forType(Browser.class).defaultResize(DefaultResize.BOTH);
		forType(Canvas.class).defaultResize(DefaultResize.BOTH);
		forType(CCombo.class).defaultResize(DefaultResize.X_AXIS);
		forType(Composite.class).delay(Layout.class).defaultResize(DefaultResize.BOTH);
        forType(Combo.class).defaultResize(DefaultResize.X_AXIS);
        forType(ExpandBar.class).defaultResize(DefaultResize.BOTH);
        forType(Group.class).defaultResize(DefaultResize.BOTH);
        forType(List.class).defaultResize(DefaultResize.BOTH);
        forType(ProgressBar.class).defaultResize(DefaultResize.X_AXIS);
        forType(Scale.class).defaultResize(DefaultResize.X_AXIS);
        forType(ScrolledComposite.class).defaultResize(DefaultResize.BOTH);
        forType(Slider.class).defaultResize(DefaultResize.X_AXIS);
        forType(Spinner.class).defaultResize(DefaultResize.X_AXIS);
        forType(StyledText.class).defaultResize(DefaultResize.BOTH);
        forType(Table.class).defaultResize(DefaultResize.BOTH);
        forType(TabItem.class).typeAsMethod(Control.class, "setControl").localize("text","toolTipText");
        forType(Text.class).defaultResize(DefaultResize.BOTH);
        forType(Tree.class).defaultResize(DefaultResize.BOTH);
        forType(Widget.class).ignore(SWTBuilder.STYLE);
        
        Method method;
		try {
			method = Shell.class.getMethod("setMenuBar", Menu.class);
			forType(Shell.class).typeAsMethod(Menu.class, method);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
        
		forType(Image.class).valueHandler(ImageAsValueHandler.getInstance());
		
        //register specialized type handlers
        addTypeHandler(WidgetTypeHandler.getInstance());
        addTypeHandler(ShellHandler.getInstance());
        addTypeHandler(DialogHandler.getInstance());
        addTypeHandler(MenuItemTypeHandler.getInstance());
        
        addTypeHandler(FillLayoutHandler.getInstance());
        addTypeHandler(MigSWTLayoutHandler.getInstance());
        addTypeHandler(StackLayoutHandler.getInstance());
        
        //register property handlers
        addPropertyHandler(ButtonSelectionListenerHandler.getInstance());
        addPropertyHandler(MenuItemSelectionListenerHandler.getInstance());
        addPropertyHandler(WidgetNameHandler.getInstance());
        addPropertyHandler(SashBoundsHandler.getInstance());
        //addPropertyHandler(SashFormWeightHandler.getInstance());
        addPropertyHandler(LayoutNameHandler.getInstance());
        
	}

	/* (non-Javadoc)
	 * @see org.javabuilders.BuilderConfig#createPropertyChangeSupport(java.lang.Object)
	 */
	@Override
	public PropertyChangeSupport createPropertyChangeSupport(Object source) {
		return new org.javabuilders.swt.worker.SWTPropertyChangeSupport(source, Display.getDefault());
	}
	
	/**
	 * SWT implementation of $confirm
	 */
	private static class ConfirmCommand implements ICustomCommand<Boolean>  {
		public Boolean process(BuildResult result, Object source) {
			return MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
					Builder.getResourceBundle().getString("title.confirmation"),
					Builder.getResourceBundle().getString("question.areYouSure"));
		}
	}
}
