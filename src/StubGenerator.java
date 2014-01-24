import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import javax.tools.DiagnosticCollector;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class StubGenerator {

	private String className; // name of the class which we'll use for the stub
	private String stubPath; // the path of the stub to be created
	private File stubFile; // the file to be created
	private static String fileContent; // the content of the stub
	
	public StubGenerator(String name) {
		this.className = name;
		this.stubPath = "/home/qglinelm/workspace/etape2/src/";
		this.stubFile = new File(stubPath+className+"_stub.java");
		this.fileContent ="";
	}
	// Generating the stub file 
	public void writeStub() {
		try {
			// retrieving the class methods
			Class class0 = Class.forName(getClassName());
			Method[] methods = class0.getMethods();

			// Writing header and constructor
			fileContent     += "public class " +className+"_stub" + " extends SharedObject implements " +className+"_itf {\n\n"
			                      + "\tpublic " +className+"_stub(Object o,int id) { \n"
			                      + "\t\tsuper(o,id);\n"
			                      + "\t}\n\n";
			
			// Writing methods
			for(int i = 0;i<methods.length;i++) {
				Method m = methods[i];
				String methodName = m.getName();
				String returnType = m.getReturnType().getSimpleName();
				String declaringClass = m.getDeclaringClass().toString().substring(6); // keeping class name only
				// Filtering pertinent methods
				if (className.equals(declaringClass)){
					
					// System.out.println("method name : " +methodName+"\n return type : "+returnType);
					Class<?>[] parameters = m.getParameterTypes();
					ArrayList<String> parametersType = new ArrayList<String> ();
					
					
					String signature ="(";
					for (int j = 0; j < parameters.length ; j++) {
						// System.out.println(parameters[j].getSimpleName());
						parametersType.add(parameters[j].getSimpleName());
						signature += parameters[j].getSimpleName()+" _param";
						if (j!=parameters.length-1) {
							signature +=" , ";
						}
					}
					signature += ")";
					fileContent += "\t@Override\n" + "\tpublic " +returnType+" "+methodName + signature+" {\n\t\t";
				
					// casting of the shared object
					fileContent +=  this.className + " obj" +className+ " = " + "(" + this.className +") obj;\n";
					
					// void methods
					if (!returnType.equals("void")) {
						fileContent += "\t\treturn obj"+className+".read();\n\t}\n\n";
					} 
					// methods that have a non void return type
					else {
						fileContent += "\t\tobj"+className+".write(_param);\n\t}\n\n";
					}
				}
			}
		fileContent += "}";
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	// Generate the file with fileContent
	public void generateFile() {
		
		/*PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter( this.stubPath +className+"_stub.java"));
			out.println(fileContent);
		    out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		try {
			stubFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(stubFile);			
			fos.write(fileContent.getBytes()); 
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	// Compile the file generated : jdk needed
	public void compileStub() {

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();	    
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(stubFile));	    
		compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnit).call();	    	    
		
		for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {	    
		   System.out.println("Error on line "+ diagnostic.getLineNumber()+" in "+diagnostic.getSource());	    
		}
	}
	
	// Getters and Setters
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getStubPath() {
		return stubPath;
	}

	public void setStubPath(String path) {
		this.stubPath = path;
	}

	public static String getFileContent() {
		return fileContent;
	}

	public static void setFileContent(String fileContent) {
		StubGenerator.fileContent = fileContent;
	}
	
	// testing
	public static void main(String args []) {
		StubGenerator sg = new StubGenerator("Sentence");
		sg.writeStub();
		sg.generateFile();
		sg.compileStub();
		System.err.println(fileContent);
	}	
}
