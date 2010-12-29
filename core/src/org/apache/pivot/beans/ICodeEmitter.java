/*
 * author: calathus
 * create date: 12/24/2010
 */
package org.apache.pivot.beans;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public interface ICodeEmitter {

    void code_start();

    void code_end();

    void code_declare(Class<?> type, Object obj);

    void code_new_root(final Class<?> type);

    void code_new(String element_type, String name, Object parent_value, Object element_value);

    void code_set_attr(Object obj, String name, Class<?> propertyClass, Object value);

    void code_static_set_attr(Method setterMethod, Object object, Object value);

    void code_bind(Field field, Object object, Object value);

    void code_block_end(String name, String elementType);

    void code_comment(String name, String elementType);

    void code_node_id(String name);
    //void code_get_node(String name);

    void register_reference(String name, Object value);

    //
    //
    //
    void inc();

    void dec();

    int call_depth();

    void flush(); // ???
}
