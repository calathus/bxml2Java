/*
 * author: calathus
 * create date: 12/24/2010
 */

package org.apache.pivot.beans;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.beans.ICodeEmitter;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;

public class CodeEmitterDeclarativeStyle implements ICodeEmitter {
    private final CodeEmitterContext ctx;
    private final String packageName;
    private final String targetClassName;
    private final File bxmlfile;

    private final PrintWriter wr;

    private int call_depth = 0;

    int element_index = 2;

    public CodeEmitterDeclarativeStyle(File bxmlfile, String packageName, String targetClassName, PrintWriter wr) {
        this.ctx = new CodeEmitterContext(packageName);
        this.packageName = packageName;
        this.targetClassName = targetClassName;
        this.wr = wr;
        this.bxmlfile = bxmlfile;
    }

    public CodeEmitterDeclarativeStyle(File bxmlfile, String packageName, String targetClassName, OutputStream out) {
        this(bxmlfile, packageName, targetClassName, new PrintWriter(out));
    }

    public int call_depth() {
        return call_depth;
    }

    //
    public void code_start() {
        if (call_depth == 0) {
            String bxmlFileNameExp = "\""+bxmlfile.getName().replace("\"", "\\\"")+"\"";
            pr0("package " + packageName + ";");
            pr0();
            pr0("import java.net.*;");
            pr0("import org.apache.pivot.wtk.converter.CodeEmitterRuntime;");
            pr0("import org.apache.pivot.wtk.*;");
            pr0("import org.apache.pivot.wtk.content.*;");
            pr0("import org.apache.pivot.wtk.effects.easing.*;");
            pr0("import org.apache.pivot.wtk.effects.*;");
            pr0("import org.apache.pivot.wtk.media.*;");
            pr0("import org.apache.pivot.wtk.skin.*;");
            pr0("import org.apache.pivot.wtk.text.*;");
            pr0("import org.apache.pivot.wtk.validation.*;");
            pr0("import org.apache.pivot.collections.adapter.*;");
            pr0("import org.apache.pivot.collections.concurrent.*;");
            pr0("import org.apache.pivot.collections.immutable.*;");
            pr0("import org.apache.pivot.collections.*;");
            pr0("import org.apache.pivot.util.Resources;");
            pr0("import org.apache.pivot.beans.Bindable;");
            pr0();
            pr0("public class " + targetClassName + " implements Application {");
            pr0("    private Window window = null;");
            pr0();
            pr0("    @Override");
            pr0("    public void startup(Display display, Map<String, String> properties) throws Exception {");
            pr0("        //BXMLSerializer bxmlSerializer = new BXMLSerializer();");
            pr0("        //window = (Window)bxmlSerializer.readObject(getClass().getResource("+bxmlFileNameExp+"));");
            pr0("        final Object obj = createROOT();");
            pr0("        if (obj instanceof Window) {;");
            pr0("            window = (Window)obj;");
            pr0("        } else if (obj instanceof Component) {");
            pr0("            window = new Window();");
            pr0("            window.setContent((Component)obj);");
            pr0("            window.setTitle("+bxmlFileNameExp+");");
            pr0("        } else {");
            pr0("            System.out.println(\"getComponent returned object with type: \"+obj.getClass());");
            pr0("        }");
            pr0("        window.open(display);");
            pr0("    }");
            pr0();
            pr0("    @Override");
            pr0("    public boolean shutdown(boolean optional) {");
            pr0("        if (window != null) {");
            pr0("            window.close();");
            pr0("        }");
            pr0();
            pr0("        return false;");
            pr0("    }");
            pr0();
            pr0("    @Override");
            pr0("    public void suspend() {");
            pr0("    }");
            pr0();
            pr0("    @Override");
            pr0("    public void resume() {");
            pr0("    }");
            pr0();
            pr0("    public static void main(String[] args) {");
            pr0("        DesktopApplicationContext.main("+targetClassName+".class, args);");
            pr0("    }");
            pr0();
            pr0("    public Object createROOT() {");
            pr0("        ROOT root = new ROOT();");
            pr0("        Map<String, Object> namespace = new HashMap<String, Object>();");
            pr0("        URL location = null;");
            pr0("        Resources resources = null;");
            pr0();
            pr0("        // Bind the root to the namespace");
            pr0("        if (root instanceof Bindable) {");
            pr0("            Bindable bindable = (Bindable) root;");
            pr0("            bindable.initialize(namespace, location, resources);");
            pr0("        }");
            pr0("        return root;");
            pr0("    }");

            pr0();
        }
        call_depth++;
    }

    public void code_end() {
        call_depth--;
        if (call_depth == 0) {
            pr0("        } catch (Exception e) {");
            pr0("            e.printStackTrace();");
            pr0("            throw new RuntimeException(e);");
            pr0("        }");
            pr0("    }}");
            pr0("}");
            wr.flush();
        }
    }

    //
    //
    //
    public void code_declare(final Class<?> type, final Object obj) {
        final String var = ctx.env.declare(obj);
        final String clsName = ctx.getShortClassName(type);
        dec();
        pr("final "+clsName + " " + var + " = (new " + clsName + "() {{");
        inc();
    }

    public void code_new_root(final Class<?> type) {
        dec();dec();
        pr("static class ROOT extends "+ctx.getShortClassName(type)+" {{");
        inc();
        pr("try {");
        //pr("final Object root = ", "(new "+ctx.getShortClassName(type)+"() {{");
    }

    public void code_new(String element_type, String name, Object parent_value, Object element_value) {
        ///System.out.println(">>>1 code_new element_type: "+element_type+", name: "+name+", parent_value: "+parent_value+", element_value: "+element_value);
        final Class<?> type = element_value.getClass();
        if (element_type.equals("WRITABLE_PROPERTY")) {
            String setterName = getSetterName(name);
            code_new(setterName, type);
            return;
        } else if (element_type.equals("READ_ONLY_PROPERTY")) {
            String getterName = getGetterName(name);
            code_new(getterName+"().add", type);
            return;
        } else if (element_type.equals("INCLUDE")) {
            //String getterName = ctx.getGetterName(name);
            code_new("add", type); // TODO...
            return;
        }
        Class<?> parentType = parent_value.getClass();
        DefaultProperty defaultProperty = parentType.getAnnotation(DefaultProperty.class);

        if (defaultProperty == null) {
            if (parent_value instanceof Sequence<?>) {
                code_new("add", type);
            } else {
                throw new RuntimeException(parent_value.getClass() + " is not a sequence.");
            }
        } else {
            String defaultPropertyName = defaultProperty.value();
            BeanAdapter beanAdapter = new BeanAdapter(parent_value);
            Object defaultPropertyValue = beanAdapter.get(defaultPropertyName);
            ///System.out.println(">>>2 code_new element_type: "+element_type+", name: "+name+", defaultPropertyName: "+defaultPropertyName+", "+defaultPropertyValue);

            if (defaultPropertyValue instanceof Sequence<?>) {
                String getterName = getGetterName(defaultPropertyName);
                code_new(getterName+"().add", type);
            } else {
                String setterName = getSetterName(defaultPropertyName);
                code_new(setterName, type);
            }
        }
    }
    private void code_new(String opr, final Class<?> type) {
        pr(opr, "(new "+ctx.getShortClassName(type)+"() {{");
    }

    public void code_block_end(String name, String elementType) {
        pr("}}); // "+elementType+", name: "+name);
    }

    public void code_comment(String name, String elementType) {
        pr("// "+name+"(): "+elementType);
    }

    public void code_set_attr(final Object obj, final String name, Class<?> propertyClass, final Object value) {
        if (obj == null) {
            throw new RuntimeException("[code_set_attr] obj must not be null: name: "+name+", value: "+value);
        }

        if (obj instanceof Dictionary<?, ?>) {
            final Dictionary<?, ?> dictionary = (Dictionary<?, ?>)obj;
            if (propertyClass == null) {
                propertyClass = (Class<?>)ctx.findDictionaryValueType(dictionary);
            }
            if (dictionary instanceof BeanAdapter) {
                code_bean_setter0(((BeanAdapter)dictionary).getBean(), getSetterName(name), propertyClass, value);
            } else {
                code_put_item(dictionary, name, propertyClass, value);
            }
        } else {
            final String setterName = getSetterName(name);
            code_set_prop(obj, setterName, value);
        }
    }

    //
    private void code_set_prop(Object obj, String setterName, Object value) {
        final Class<?> propertyClass = ctx.getSetterType(obj, setterName);
        if (propertyClass == null) {
            // this happen when parent attr is referenced..
            //System.out.println("[code_set_prop] no setter fund for "+setterName+", "+obj);
            return;
        }
        code_bean_setter0(obj, setterName, propertyClass, value);
    }

    private void code_put_item(final Dictionary<?, ?> dictionary, final String attrName, Class<?> propertyClass, final Object attrValue) {
        if (propertyClass == null)  propertyClass = (Class<?>)ctx.findDictionaryValueType(dictionary);
        final String vexp = ctx.getValueExp(propertyClass, attrValue);
        pr("put(\"" + attrName + "\", "+vexp+");");
    }

    private void code_bean_setter0(final Object obj, String setterName, Class<?> propertyClass, Object value) {
        //String var = ctx.(propertyClass, value);
        String vexp = ctx.getValueExp(propertyClass, value);
        if (vexp.endsWith("*")) {
            vexp = vexp.substring(0, vexp.length()-1);
            pr(setterName + "(" + vexp +", true);");
        } else {
            pr(setterName + "(" + vexp + ");");
        }
    }

    //
    public void code_static_set_attr(final Method setterMethod, final Object object, final Object value) {
        final Class<?> cls = setterMethod.getParameterTypes()[0];
        final String vexp = ctx.getValueExp(cls, value);
        final String setterName = setterMethod.getName();
        final Class<?> dclCls = setterMethod.getDeclaringClass();
        final String className = ctx.getShortClassName(dclCls);

        inc();
        pr(className + "." + setterName + "(this, "+ vexp + ");");
        dec();
    }
    public void code_node_id(String name) {
        inc();
        pr("CodeEmitterRuntime.register(\""+name+"\", this);");
        dec();
    }
    //public void code_get_node(String name) {
    //    pr("CodeEmitterRuntime.getNodeValue(\""+name+"\");");
    //}
    public void register_reference(String name, Object value) {
        ctx.register_reference(name, value);
    }

    public void code_bind(final Field field, final Object object, final Object value) {
        //final String var = ctx.getVarExp(object);
        //final String vexp0 = ctx.getVarExp(object);
        final String vexp = ctx.getValueExp(value);
        final String fieldName = field.getName();
        inc();
        inc();
        pr("CodeEmitterRuntime.bind(ROOT.this, \""+fieldName+"\");");
        dec();
        dec();
    }

    //
    //
    //
    private String getSetterName(final String name) {
        String setterName = "set" + getHeadLowCaseName(name);
        if (setterName.equals("setTableView")) {
            //throw new RuntimeException();
        }
        return setterName;
    }

    private String getGetterName(final String name) {
        return "get" + getHeadLowCaseName(name);
    }

    private String getHeadLowCaseName(final String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    //
    //
    //
    public void inc() {
        element_index++;
    }
    public void dec() {
        element_index--;
    }
    void tab(int level) {
        for (int i = 0; i < level; i++) {
            wr.print("    ");
        }
    }
    void tab(String s) {
        tab(element_index);
    }
    void pr(String s1, String s2) {
        tab(element_index);
        wr.print(s1);
        wr.println(s2);
    }
    void pr(String s) {
        tab(element_index);
        wr.println(s);
    }

    private void pr0(String s) {
        wr.println(s);
    }
    private void pr0() {
        wr.println();
    }
    public void flush() {
        wr.flush();
    }

    /*
        public void code_set_element(final Object obj, final String name, final Object value) {
            if (obj == null) {
                throw new RuntimeException("[code_set_attr] obj must not be null: name: "+name+", value: "+value);
            }

            if (obj instanceof Dictionary<?, ?>) {
                final Dictionary<?, ?> dictionary = (Dictionary<?, ?>)obj;
                if (dictionary instanceof BeanAdapter) {
                    final String setterName = ctx.getSetterName(name);
                    code_bean_setter0(((BeanAdapter)dictionary).getBean(), setterName, null, value);
                    codes.set(setterName);
                } else {
                    code_put_item(dictionary, name, null, value);
                    codes.put(name);
                }
            } else {
                final String setterName = ctx.getSetterName(name);
                code_set_prop(obj, setterName, value);
                codes.set(setterName);
            }
            codes.pr("}});", indent_level);
        }

        public void code_add_element(final Sequence<?> sequence, final Object value) {
            codes.pr("}});", indent_level);
            codes.add();
        }
    */

    /*

    public void code_new_set(String name, Object value) {
        code_new(ctx.getSetterName(name), value.getClass());
    }
    // TODO, get sequence getter!
    public boolean code_new_add_default(String name, Object parent_value, Object value) {

        final Class<?> type = value.getClass();
        //if (parent_value instanceof Sequence<?>) {
            Class<?> parentType = parent_value.getClass();
            DefaultProperty defaultProperty = parentType.getAnnotation(DefaultProperty.class);
            String defaultPropertyName = defaultProperty.value();
            BeanAdapter beanAdapter = new BeanAdapter(parent_value);
            Object defaultPropertyValue = beanAdapter.get(defaultPropertyName);



            if (defaultPropertyValue instanceof Sequence<?>) {
                String getterName = ctx.getGetterName(defaultPropertyName);
                code_new(getterName+"().add", type);
            } else {
                String setterName = ctx.getSetterName(defaultPropertyName);
                code_new(setterName, type);
            }
            //code_new(opr+"().add", type);
            return true;
        //}
        //return false;
    }
    public void code_new_add(final Class<?> type) {
        code_new("add", type);
    }
    private void code_new(String opr, final Class<?> type) {
        pr(opr, "(new "+ctx.getShortClassName(type)+"() {{");
    }
    */
    /*
    // use pivot convention..
    private String findGetSequnceMethodName(Object obj, final Class<?> type) {
        String clasName = type.getName();
        if (clasName.startsWith("org.apache.pivot.") && clasName.contains("$")) {
            return "get"+type.getSimpleName()+"s";
        }
        return null;
    }
    */
    /*
    public void code_new(final Object parent_value, final Object element_value) {
        final Class<?> type = element_value.getClass();
        String opr = getMethod(parent_value, element_value);
        pr(opr, "(new "+ctx.getShortClassName(type)+"() {{");
    }

    private String getMethod(Object parent_value, Object element_value) {
        // If the parent element has a default property, use it; otherwise, if the
        // parent is a sequence, add the element to it
        Class<?> parentType = parent_value.getClass();
        DefaultProperty defaultProperty = parentType.getAnnotation(DefaultProperty.class);

        if (defaultProperty == null) {
            if (parent_value instanceof Sequence<?>) {
                //Sequence<Object> sequence = (Sequence<Object>)parent_value;
                //sequence.add(element_value);
                return "add";
            } else {
                throw new RuntimeException(parent_value.getClass() + " is not a sequence.");
            }
        } else {
            String defaultPropertyName = defaultProperty.value();
            BeanAdapter beanAdapter = new BeanAdapter(parent_value);
            Object defaultPropertyValue = beanAdapter.get(defaultPropertyName);
            if (defaultPropertyValue instanceof Sequence<?>) {
                String getterName = ctx.getGetterName(defaultPropertyName);
                return getterName+"().add";
            } else {
                String setterName = ctx.getSetterName(defaultPropertyName);
                return setterName;
            }
        }
    }
    */


}
