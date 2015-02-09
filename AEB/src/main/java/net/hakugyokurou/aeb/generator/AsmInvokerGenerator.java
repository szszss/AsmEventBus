package net.hakugyokurou.aeb.generator;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.hakugyokurou.aeb.EventInvoker;
import net.hakugyokurou.aeb.exception.AEBRegisterException;

public class AsmInvokerGenerator implements InvokerGenerator {

	private final static int   HEADER_MAGIC   = 0xCAFEBABE; //Magic number
	private final static short HEADER_VERSION = 49;         //Java1.5
	private final static byte CONSTANT_TAG_UTF8   = 1;     //UTF8_info
	private final static byte CONSTANT_TAG_CLASS  = 7;     //Class_info
	private final static byte CONSTANT_TAG_METHOD = 10;    //Methodref_info
	private final static byte CONSTANT_TAG_NAT    = 12;    //NameAndType_info
	private final static byte[] CONSTANT_CLASSNAME_PARENT = EventInvoker.class.getName().replace('.', '/').getBytes();
	private final static byte[] CONSTANT_METHOD_INIT_NAME   = "<init>".getBytes();
	private final static byte[] CONSTANT_METHOD_INIT_DESC   = "(Ljava/lang/reflect/Method;)V".getBytes();
	private final static byte[] CONSTANT_METHOD_INVOKE_NAME = "invoke".getBytes();
	private final static byte[] CONSTANT_METHOD_INVOKE_DESC = "(Ljava/lang/Object;Ljava/lang/Object;)V".getBytes();
	private final static byte[] CONSTANT_ATTR_LNT  = "LineNumberTable".getBytes();
	private final static byte[] CONSTANT_ATTR_LVT  = "LocalVariableTable".getBytes();
	private final static byte[] CONSTANT_ATTR_CODE = "Code".getBytes();
	private final static byte[] CONSTANT_ATTR_EXCEPTION = "Exceptions".getBytes();
	private final static byte[] CONSTANT_VAR_THIS       = "this".getBytes();
	private final static byte[] CONSTANT_VAR_SUBSCRIBER = "subscriber".getBytes();
	private final static byte[] CONSTANT_VAR_METHODDESC = "Ljava/lang/reflect/Method;".getBytes();
	private final static byte[] CONSTANT_VAR_OBJECT     = "Ljava/lang/Object;".getBytes();
	private final static byte[] CONSTANT_VAR_THROWABLE  = "java/lang/Throwable".getBytes();
	private final static byte[] CONSTANT_VAR_RECEIVER   = "receiver".getBytes();
	private final static byte[] CONSTANT_VAR_EVENT      = "event".getBytes();
	
	private final static byte[] HEADER;
	private final static byte[] CONSTANTPOOL;
	private final static byte[] ZLICH = { 0, 1,  //Access: public
										  0, 10, //Class name
										  0, 19, //Parent name
										  0, 0,  //Interface amount
										  0, 0,  //Field amount
										  0, 2 };//Method amount
	private final static byte[] METHOD_INIT = { 
		0, 1, //public
		0, 21, //Name:#21 <init>
		0, 22, //Desc:#22 (Ljava/lang/reflect/Method;)V
		0, 1, //1 attr
			0, 28, //ATTR: Code
			0, 0, 0, 62, //Length: 62 bytes
			0, 2, //Max stack: 2
			0, 2, //Max local var: 2
			0, 0, 0, 6, //Code length: 6 bytes
			42, 43, -73, 0, 8, -79, //Code: invoke super(subscriber);
			0, 0,  //No exception
			0, 2,  //2 attrs
				0, 29, //ATTR: LocalVarTable
				0, 0, 0, 22, //Length: 22 bytes
				0, 2, //2 vars
					0, 0, 
					0, 6, 
					0, 9, 
					0, 2, 
					0, 0, 
					
					0, 0, 
					0, 6, 
					0, 11, 
					0, 12, 
					0, 1, 
				0, 30, //ATTR: LineNumberTable
				0, 0, 0, 10, //Length: 10 bytes
				0, 2, // 2 lines
					0, 0, 
					0, 27, 
	
					0, 5, 
					0, 28
	};
	private final static byte[] METHOD_INVOKE_PART1 = { 
		0, 1, //public
		0, 13, //Name:#13 invoke
		0, 14, //Desc:#14 (Ljava/lang/Object;Ljava/lang/Object;)V
		0, 2, //2 attrs
			0, 28, //ATTR: Code
			0, 0, 0, 78, //Length: 78 bytes
			0, 2, //Max stack: 2
			0, 3, //Max local var: 3
			0, 0, 0, 12 //Code length: 12 bytes
	};
	private final static byte[] METHOD_INVOKE_PART2_NONSTATIC = { 
			43, -64, 0, 18, 44, -64, 0, 20, -74, 0, 24, -79 //Code: cast and invoke reciver.subscriber(castedEvent);
	};
	private final static byte[] METHOD_INVOKE_PART2_STATIC = { 
			0, 0, 0, 0, 44, -64, 0, 20, -72, 0, 24, -79 //Code: cast and invoke ReciverClass.subscriber(castedEvent);
	};
	private final static byte[] METHOD_INVOKE_PART3 = { 
			0, 0,  //No exception
			0, 2,  //2 attrs
				0, 29, //ATTR: LocalVarTable
				0, 0, 0, 32, //Length: 32 bytes
				0, 3, //3 vars
					0, 0,
					0, 12,
					0, 9,
					0, 2,
					0, 0,
					
					0, 0,
					0, 12,
					0, 25,
					0, 26,
					0, 1,
	
					0, 0,
					0, 12,
					0, 27,
					0, 26,
					0, 2,
				0, 30,  //ATTR: LineNumberTable
				0, 0, 0, 10, //Length: 10 bytes
				0, 2, // 2 lines
					0, 0, 
					0, 7, 
					
					0, 11, 
					0, 8, 
			0, 31, //ATTR: Exceptions
			0, 0, 
			0, 4, 
			0, 1, 
			0, 16, 
			0, 0
	};
	
	static {
		ByteBuffer buffer = ByteBuffer.allocate(512);
		{
			buffer.putInt(HEADER_MAGIC).putShort((short) 0).putShort(HEADER_VERSION);
			buffer.flip(); 
			buffer.get(HEADER = new byte[buffer.limit()]);
			buffer.clear();
		}
		{
			//Constant Pool
			/*#7 */buffer.put(CONSTANT_TAG_NAT).putShort((short) 21).putShort((short) 22);
			/*#8 */buffer.put(CONSTANT_TAG_METHOD).putShort((short) 19).putShort((short) 7);
			/*#9 */buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_VAR_THIS.length).put(CONSTANT_VAR_THIS);
			/*#10*/buffer.put(CONSTANT_TAG_CLASS).putShort((short) 1); //Ref of this class
			/*#11*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_VAR_SUBSCRIBER.length).put(CONSTANT_VAR_SUBSCRIBER);
			/*#12*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_VAR_METHODDESC.length).put(CONSTANT_VAR_METHODDESC);
			/*#13*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_METHOD_INVOKE_NAME.length).put(CONSTANT_METHOD_INVOKE_NAME);
			/*#14*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_METHOD_INVOKE_DESC.length).put(CONSTANT_METHOD_INVOKE_DESC);
			/*#15*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_VAR_THROWABLE.length).put(CONSTANT_VAR_THROWABLE);
			/*#16*/buffer.put(CONSTANT_TAG_CLASS).putShort((short) 15); //Ref of Throwable
			/*#17*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_CLASSNAME_PARENT.length).put(CONSTANT_CLASSNAME_PARENT); //Parent name
			/*#18*/buffer.put(CONSTANT_TAG_CLASS).putShort((short) 3); //Ref of handler class
			/*#19*/buffer.put(CONSTANT_TAG_CLASS).putShort((short) 17); //Ref of parent class
			/*#20*/buffer.put(CONSTANT_TAG_CLASS).putShort((short) 4); //Ref of event class
			/*#21*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_METHOD_INIT_NAME.length).put(CONSTANT_METHOD_INIT_NAME);
			/*#22*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_METHOD_INIT_DESC.length).put(CONSTANT_METHOD_INIT_DESC);
			/*#23*/buffer.put(CONSTANT_TAG_NAT).putShort((short) 5).putShort((short) 6);
			/*#24*/buffer.put(CONSTANT_TAG_METHOD).putShort((short) 18).putShort((short) 23);
			/*#25*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_VAR_RECEIVER.length).put(CONSTANT_VAR_RECEIVER);
			/*#26*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_VAR_OBJECT.length).put(CONSTANT_VAR_OBJECT);
			/*#27*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_VAR_EVENT.length).put(CONSTANT_VAR_EVENT);
			/*#28*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_ATTR_CODE.length).put(CONSTANT_ATTR_CODE);
			/*#29*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_ATTR_LVT.length).put(CONSTANT_ATTR_LVT);
			/*#30*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_ATTR_LNT.length).put(CONSTANT_ATTR_LNT);
			/*#31*/buffer.put(CONSTANT_TAG_UTF8).putShort((short)CONSTANT_ATTR_EXCEPTION.length).put(CONSTANT_ATTR_EXCEPTION);
			buffer.flip(); 
			buffer.get(CONSTANTPOOL = new byte[buffer.limit()]);
		}
		buffer.clear();
	}
	
	public EventInvoker generateInvoker(Class<?> handler, Method subscriber,
			Class<?> event) throws AEBRegisterException {
		
		String handlerName = handler.getName().replace('.', '/');
		String eventName = event.getName().replace('.', '/');
		String subscriberName = subscriber.getName();
		String subscriberDesc = "(L" + eventName + ";)V";
		String invokerName = handlerName+"_Invoker_"+subscriberName+"_"+Math.abs(subscriber.hashCode());;
		String invokerDesc = "L" + invokerName + ";";
		
		int variableLength = invokerName.length() + 
				invokerDesc.length() + handlerName.length() + eventName.length() +
				subscriberName.length() + subscriberDesc.length() + 6 + 7*2;
		int overallLength = HEADER.length + variableLength + CONSTANTPOOL.length + ZLICH.length + METHOD_INIT.length 
				+ METHOD_INVOKE_PART1.length + METHOD_INVOKE_PART2_NONSTATIC.length + METHOD_INVOKE_PART3.length;
		int pos = 0;
		byte[] bytes = new byte[overallLength];
		//Header
		System.arraycopy(HEADER, 0, bytes, pos, HEADER.length);
		pos += HEADER.length;
		//Constant Pool
		ByteBuffer buffer = ByteBuffer.allocate(variableLength); //TODO:SHIT
		buffer.putShort((short) 32); 
		/*#1 */buffer.put(CONSTANT_TAG_UTF8).putShort((short)invokerName.length()).put(invokerName.getBytes()); //Class name
		/*#2 */buffer.put(CONSTANT_TAG_UTF8).putShort((short)invokerDesc.length()).put(invokerDesc.getBytes());
		/*#3 */buffer.put(CONSTANT_TAG_UTF8).putShort((short)handlerName.length()).put(handlerName.getBytes());
		/*#4 */buffer.put(CONSTANT_TAG_UTF8).putShort((short)eventName.length()).put(eventName.getBytes());
		/*#5 */buffer.put(CONSTANT_TAG_UTF8).putShort((short)subscriberName.length()).put(subscriberName.getBytes());
		/*#6 */buffer.put(CONSTANT_TAG_UTF8).putShort((short)subscriberDesc.length()).put(subscriberDesc.getBytes());
		buffer.flip();
		buffer.get(bytes, pos, buffer.limit());
		pos += buffer.limit();
		buffer.clear();
		System.arraycopy(CONSTANTPOOL, 0, bytes, pos, CONSTANTPOOL.length);
		pos += CONSTANTPOOL.length;
		//Zlich
		System.arraycopy(ZLICH, 0, bytes, pos, ZLICH.length);
		pos += ZLICH.length;
		//Method:<init>
		System.arraycopy(METHOD_INIT, 0, bytes, pos, METHOD_INIT.length);
		pos += METHOD_INIT.length;
		//Method:invoke
		System.arraycopy(METHOD_INVOKE_PART1, 0, bytes, pos, METHOD_INVOKE_PART1.length);
		pos += METHOD_INVOKE_PART1.length;
		if((subscriber.getModifiers() & Modifier.STATIC) > 0)
			System.arraycopy(METHOD_INVOKE_PART2_STATIC, 0, bytes, pos, METHOD_INVOKE_PART2_STATIC.length);
		else
			System.arraycopy(METHOD_INVOKE_PART2_NONSTATIC, 0, bytes, pos, METHOD_INVOKE_PART2_NONSTATIC.length);
		pos += METHOD_INVOKE_PART2_STATIC.length;  //Nonstatic one has a same length than static one
		System.arraycopy(METHOD_INVOKE_PART3, 0, bytes, pos, METHOD_INVOKE_PART3.length);
		
		Method define = null;
		Object klass;
		boolean unaccessible = false;
		//XXX:In some case, this is NOT thread-safe. For example, an another thread changes the access of define while we are invoking...
		try {
			ClassLoader cl = handler.getClassLoader();
			define = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] {String.class, byte[].class, int.class, int.class} );
			unaccessible = !define.isAccessible();
			if(unaccessible)
				define.setAccessible(true);
			klass = ((Class<?>)define.invoke(cl, invokerName.replace('/', '.'),bytes,0,bytes.length))
					.getConstructor(Method.class).newInstance(subscriber);
		} catch (Exception e) {
			throw new AEBRegisterException(e);
		} finally {
			if(unaccessible)
				define.setAccessible(false);
		}
		return (EventInvoker)klass;
	}
}

/*
 BACKUP: Old AsmInvokerGenerator which depends on ASM-lib
 
 public class AsmInvokerGenerator implements InvokerGenerator {

	protected static String CLASS_NAME_EventInvoker = EventInvoker.class.getName().replace('.', '/');
	//protected static String CLASS_NAME_Event = Event.class.getName().replace('.', '/');
	
	protected static String CONST_PARAMS = "(Ljava/lang/Object;Ljava/lang/Object;)V";
	protected static String CONST_LV = "Ljava/lang/Object;";
	
	public EventInvoker generateInvoker(Class<?> handler, Method subscriber, Class<?> event) throws AEBRegisterException {
		String handlerName = handler.getName().replace('.', '/');
		String invokerName = handlerName+"_Invoker_"+subscriber.getName()+"_"+Math.abs(subscriber.hashCode());
		String invokerName2 = invokerName.replace('/', '.'); //Too silly...Someone makes it smart, please.
		String eventName = event.getName().replace('.', '/');
		
		ClassLoader cl = handler.getClassLoader();
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, invokerName, null, CLASS_NAME_EventInvoker, null);
		{
			MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/reflect/Method;)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(27, l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CLASS_NAME_EventInvoker, "<init>", "(Ljava/lang/reflect/Method;)V", false);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(28, l1);
			mv.visitInsn(Opcodes.RETURN);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLocalVariable("this", "L"+invokerName+";", null, l0, l2, 0);
			mv.visitLocalVariable("subscriber", "Ljava/lang/reflect/Method;", null, l0, l2, 1);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "invoke", CONST_PARAMS, null, new String[] { "java/lang/Throwable" });
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(7, l0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitTypeInsn(Opcodes.CHECKCAST, handlerName);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitTypeInsn(Opcodes.CHECKCAST, eventName);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, handlerName, subscriber.getName(), "(L"+eventName+";)V", false);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(8, l1);
			mv.visitInsn(Opcodes.RETURN);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLocalVariable("this", "L"+invokerName+";", null, l0, l2, 0);
			mv.visitLocalVariable("receiver", "Ljava/lang/Object;", null, l0, l2, 1);
			mv.visitLocalVariable("event", CONST_LV, null, l0, l2, 2);
			mv.visitMaxs(2, 3);
			mv.visitEnd();
		}
		cw.visitEnd();
		//Were these written by me? Fucking of course not. They were proudly generated by Bytecode Outline!
		byte[] bytes = cw.toByteArray();
		Method define = null;
		Object klass;
		boolean unaccessible = false;
		//XXX:In some case, this is NOT thread-safe. For example, an another thread changes the access of define while we are invoking...
		try {
			define = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] {String.class, byte[].class, int.class, int.class} );
			unaccessible = !define.isAccessible();
			if(unaccessible)
				define.setAccessible(true);
			klass = ((Class<?>)define.invoke(cl, invokerName2,bytes,0,bytes.length)).getConstructor(Method.class).newInstance(subscriber);
		} catch (Exception e) {
			throw new AEBRegisterException(e);
		} finally {
			if(unaccessible)
				define.setAccessible(false);
		}
		return (EventInvoker)klass;
	}
}
 */
