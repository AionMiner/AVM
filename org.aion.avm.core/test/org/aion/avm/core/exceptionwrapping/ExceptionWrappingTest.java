package org.aion.avm.core.exceptionwrapping;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.aion.avm.core.TestClassLoader;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.Forest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;


public class ExceptionWrappingTest {
    private final Function<byte[], byte[]> commonCostBuilder = (inputBytes) -> {
        ClassReader in = new ClassReader(inputBytes);
        ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                String superclass = null;
                // TODO:  This implementation is sufficient only for this test but we will need to generalize it.
                // This implementation assumes that this is only being used because the exception table was duplicated to handle wrapper types
                // so we only check for those occurrences, then decide the common class must be throwable.
                if (type1.startsWith(CommonGenerators.kSlashWrapperClassLibraryPrefix) || type2.startsWith(CommonGenerators.kSlashWrapperClassLibraryPrefix)) {
                    superclass = "java/lang/Throwable";
                } else {
                    superclass = super.getCommonSuperClass(type1, type2);
                }
                return superclass;
            }
        };
        
        // We know that we have an exception, in this test, but the forest normally needs to be populated from a jar so manually assemble it.
        String exceptionClassSlashName = TestExceptionResource.UserDefinedException.class.getName();
        Forest<String, byte[]> classHierarchy = new Forest<>();
        Forest.Node<String, byte[]> root = new Forest.Node<>("java.lang.Object", null);
        Forest.Node<String, byte[]> parent = new Forest.Node<>("java.lang.Throwable", null);
        classHierarchy.add(root, parent);
        Forest.Node<String, byte[]> child = new Forest.Node<>(exceptionClassSlashName, null);
        classHierarchy.add(parent, child);
        Forest.Node<String, byte[]> testResource = new Forest.Node<>("org.aion.avm.core.exceptionwrapping.TestExceptionResource", null);
        classHierarchy.add(root, testResource);
        
        // WARNING:  We are using this TestHelpers.loader as a way of injecting the code we want to generate back into the TestClassLoader.
        // TODO:  Change the contract with the TestClassLoader to allow us to more easily push these in.
        // (within the shape of this unit test, we can't do much better)
        BiConsumer<String, byte[]> generatedClassesSink = (classSlashName, bytecode) -> TestHelpers.loader.addClassDirectLoad(classSlashName.replaceAll("/", "."), bytecode);
        ClassShadowing cs = new ClassShadowing(out, TestHelpers.CLASS_NAME);
        ExceptionWrapping wrapping = new ExceptionWrapping(cs, TestHelpers.CLASS_NAME, classHierarchy, generatedClassesSink);
        in.accept(wrapping, ClassReader.SKIP_DEBUG);
        
        byte[] transformed = out.toByteArray();
        return transformed;
    };

    private Class<?> testClass;

    @Before
    public void setup() throws Exception {
        TestHelpers.didUnwrap = false;
        TestHelpers.didWrap = false;
        
        String className = TestExceptionResource.class.getName();
        Map<String, byte[]> generatedClasses = CommonGenerators.generateExceptionShadowsAndWrappers();
        
        TestHelpers.loader = new TestClassLoader(TestExceptionResource.class.getClassLoader(), this.commonCostBuilder);
        byte[] raw = TestHelpers.loader.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        TestHelpers.loader.addClassForRewrite(className, raw);
        for (Map.Entry<String, byte[]> elt : generatedClasses.entrySet()) {
            TestHelpers.loader.addClassDirectLoad(elt.getKey(), elt.getValue());
        }
        
        String resourceName = className.replaceAll("\\.", "/") + "$UserDefinedException.class";
        InputStream stream = TestHelpers.loader.getParent().getResourceAsStream(resourceName);
        byte[] exceptionBytes = null;
        try {
            exceptionBytes = stream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
        String exceptionName = className + "$UserDefinedException";
        TestHelpers.loader.addClassForRewrite(exceptionName, exceptionBytes);
        // Note that we need to eagerly load all the classes provided by the user since some may cause us to generate wrappers which will be loaded
        // by other classes.
        TestHelpers.loader.loadClass(exceptionName);
        
        this.testClass = TestHelpers.loader.loadClass(className);
    }


    /**
     * Tests that a multi-catch, using only java/lang/* exception types, works correctly.
     */
    @Test
    public void testSimpleTryMultiCatchFinally() throws Exception {
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method tryMultiCatchFinally = this.testClass.getMethod("tryMultiCatchFinally");
        
        // Create an array and make sure it is correct.
        Assert.assertFalse(TestHelpers.didUnwrap);
        int result = (Integer) tryMultiCatchFinally.invoke(null);
        Assert.assertTrue(TestHelpers.didUnwrap);
        Assert.assertEquals(3, result);
    }

    /**
     * Tests that a manually creating and throwing a java/lang/* exception type works correctly.
     */
    @Test
    public void testmSimpleManuallyThrowNull() throws Exception {
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method manuallyThrowNull = this.testClass.getMethod("manuallyThrowNull");
        
        // Create an array and make sure it is correct.
        Assert.assertFalse(TestHelpers.didWrap);
        boolean didCatch = false;
        try {
            manuallyThrowNull.invoke(null);
        } catch (InvocationTargetException e) {
            // Make sure that this is the wrapper type that we normally expect to see.
            Class<?> compare = TestHelpers.loader.loadClass("org.aion.avm.exceptionwrapper.java.lang.NullPointerException");
            didCatch = e.getCause().getClass() == compare;
        }
        Assert.assertTrue(TestHelpers.didWrap);
        Assert.assertTrue(didCatch);
    }

    /**
     * Tests that we can correctly interact with exceptions from the java/lang/* hierarchy from within the catch block.
     */
    @Test
    public void testSimpleTryMultiCatchInteraction() throws Exception {
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method tryMultiCatchFinally = this.testClass.getMethod("tryMultiCatch");
        
        // Create an array and make sure it is correct.
        Assert.assertFalse(TestHelpers.didUnwrap);
        int result = (Integer) tryMultiCatchFinally.invoke(null);
        Assert.assertTrue(TestHelpers.didUnwrap);
        Assert.assertEquals(2, result);
    }

    /**
     * Tests that we can re-throw VM-generated exceptions and re-catch them.
     */
    @Test
    public void testRecatchCoreException() throws Exception {
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method outerCatch = this.testClass.getMethod("outerCatch");
        
        // Create an array and make sure it is correct.
        Assert.assertFalse(TestHelpers.didUnwrap);
        int result = (Integer) outerCatch.invoke(null);
        Assert.assertTrue(TestHelpers.didUnwrap);
        // 3 here will imply that the exception table wasn't re-written (since it only caught at the top-level Throwable).
        Assert.assertEquals(2, result);
    }


    public static class TestHelpers {
        public static final String CLASS_NAME = TestHelpers.class.getName().replaceAll("\\.", "/");
        public static int countWrappedClasses;
        public static int countWrappedStrings;
        public static boolean didUnwrap = false;
        public static boolean didWrap = false;
        public static TestClassLoader loader = null;
        
        public static <T> org.aion.avm.java.lang.Class<T> wrapAsClass(Class<T> input) {
            countWrappedClasses += 1;
            return new org.aion.avm.java.lang.Class<T>(input);
        }
        public static org.aion.avm.java.lang.String wrapAsString(String input) {
            countWrappedStrings += 1;
            return new org.aion.avm.java.lang.String(input);
        }
        public static org.aion.avm.java.lang.Object unwrapThrowable(Throwable t) {
            org.aion.avm.java.lang.Object shadow = null;
            try {
                // NOTE:  This is called for both the cases where the throwable is a VM-generated "java.lang" exception or one of our wrappers.
                // We need to wrap the java.lang instance in a shadow and unwrap the other case to return the shadow.
                String throwableName = t.getClass().getName();
                if (throwableName.startsWith("java.lang.")) {
                    // This is VM-generated - we will have to instantiate a shadow, directly.
                    shadow = convertVmGeneratedException(t);
                } else {
                    // This is one of our wrappers.
                    org.aion.avm.exceptionwrapper.java.lang.Throwable wrapper = (org.aion.avm.exceptionwrapper.java.lang.Throwable)t;
                    shadow = (org.aion.avm.java.lang.Object)wrapper.unwrap();
                }
                didUnwrap = true;
            } catch (Throwable err) {
                // Unrecoverable internal error.
                org.aion.avm.core.util.Assert.unexpected(err);
            }
            return shadow;
        }
        public static Throwable wrapAsThrowable(org.aion.avm.java.lang.Object arg) {
            Throwable result = null;
            try {
                // In this case, we just want to look up the appropriate wrapper (using reflection) and instantiate a wrapper for this.
                String objectClass = arg.getClass().getName();
                // We know that this MUST be one of our shadow objects.
                org.aion.avm.core.util.Assert.assertTrue(objectClass.startsWith(CommonGenerators.kShadowClassLibraryPrefix));
                String wrapperClassName = CommonGenerators.kWrapperClassLibraryPrefix + objectClass.substring(CommonGenerators.kShadowClassLibraryPrefix.length());
                Class<?> wrapperClass = loader.loadClass(wrapperClassName);
                result = (Throwable)wrapperClass.getConstructor(Object.class).newInstance(arg);
                didWrap = true;
            } catch (Throwable err) {
                // Unrecoverable internal error.
                org.aion.avm.core.util.Assert.unexpected(err);
            } 
            return result;
        }
        private static org.aion.avm.java.lang.Throwable convertVmGeneratedException(Throwable t) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
            // First step is to convert the message and cause into shadow objects, as well.
            String originalMessage = t.getMessage();
            org.aion.avm.java.lang.String message = (null != originalMessage)
                    ? wrapAsString(originalMessage)
                    : null;
            Throwable originalCause = t.getCause();
            org.aion.avm.java.lang.Throwable cause = (null != originalCause)
                    ? convertVmGeneratedException(originalCause)
                    : null;
            
            // Then, use reflection to find the appropriate wrapper.
            String throwableName = t.getClass().getName();
            Class<?> shadowClass = loader.loadClass(CommonGenerators.kShadowClassLibraryPrefix + throwableName);
            return (org.aion.avm.java.lang.Throwable)shadowClass.getConstructor(org.aion.avm.java.lang.String.class, org.aion.avm.java.lang.Throwable.class).newInstance(message, cause);
        }
    }

}