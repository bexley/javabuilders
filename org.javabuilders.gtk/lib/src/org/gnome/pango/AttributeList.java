/*
 * AttributeList.java
 *
 * Copyright (c) 2007-2009 Operational Dynamics Consulting Pty Ltd
 *
 * The code in this file, and the library it is a part of, are made available
 * to you by the authors under the terms of the "GNU General Public Licence,
 * version 2" plus the "Classpath Exception" (you may link to this code as a
 * library into other programs provided you don't make a derivation of it).
 * See the LICENCE file for the terms governing usage and redistribution.
 */
package org.gnome.pango;

import org.gnome.glib.Boxed;

/**
 * A list of Attributes that will be applied to a piece of text.
 * 
 * <p>
 * Used as follows. First, build your text up into a String and initialize the
 * Layout with it. You'll also have to create the wrapper that will hold the
 * list of Attributes that will be applied to that Layout.
 * 
 * <pre>
 * layout = new Layout(cr);
 * layout.setText(str);
 * 
 * list = new AttributeList();
 * </pre>
 * 
 * Then, iterate over your text and for each span where you want formatting,
 * create one or more Attributes and specify the ranges that each will apply.
 * For instance, for a word starting at offset <code>15</code> and being
 * <code>4</code> characters wide.
 * 
 * <pre>
 * attr = new StyleAttribute(Style.ITALIC);
 * attr.setIndexes(layout, 15, 4);
 * list.insert(attr);
 * 
 * attr = new ForegroundColorAttribute(0.1, 0.5, 0.9);
 * attr.setIndexes(layout, 15, 4);
 * list.insert(attr);
 * </pre>
 * 
 * etc, adding each one to the AttributeList. Finally, tell the Layout to use
 * that list:
 * 
 * <pre>
 * layout.setAttributes(list);
 * </pre>
 * 
 * and you're on your way.
 * 
 * @author Andrew Cowie
 * @since 4.0.10
 */
/*
 * AttrList was the original mapping, but since we aren't exposing
 * pango_attr_... anything, and DO have Attribute as the public class for
 * PangoAttribute pointers, renaming to this to AttributeList makes it much
 * clearer.
 */
public final class AttributeList extends Boxed
{
    protected AttributeList(long pointer) {
        super(pointer);
    }

    /**
     * Create an AttributeList. You'll want a new one for each paragraph of
     * text you render.
     * 
     * @since 4.0.10
     */
    public AttributeList() {
        super(PangoAttrList.createAttributeList());
    }

    protected void release() {
        PangoAttrList.unref(this);
    }

    /**
     * Insert an Attribute into this list. It will be inserted after any other
     * Attributes already in the list possessing the same <var>start
     * index</var>.
     * 
     * @since 4.0.10
     */
    public void insert(Attribute attr) {
        PangoAttrList.insert(this, attr);
    }

    /**
     * Insert an Attribute into this list. Same as {@link #insert(Attribute)
     * insert()} except that the Attribute will come before other Attributes
     * already in the list possessing the same <var>start index</var>.
     * 
     * @since 4.0.10
     */
    public void insertBefore(Attribute attr) {
        PangoAttrList.insertBefore(this, attr);
    }
}
