package edu.nju.tools.jdt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.springframework.stereotype.Component;

import edu.nju.entity.Metric;

/**
 * 使用JDT（Java Development Tools）对代码进行静态分析
 * 
 * @author SuZiquan
 *
 */
@Component
public class JDTAnalyzer {

	@SuppressWarnings("unchecked")
	public Metric parseJavaFile(String fileName, String content) {

		int totalLineCount = 0;
		int commentLineCount = 0;
		int fieldCount = 0;
		int methodCount = 0;
		int maxCoc = 0;

		String[] sources = {};
		String[] classpath = {};
		String[] encodings = {};

		ASTParser parser = ASTParser.newParser(AST.JLS8);

		parser.setUnitName(fileName);
		parser.setSource(content.toCharArray());

		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setCompilerOptions(JavaCore.getOptions());

		parser.setEnvironment(classpath, sources, encodings, false);

		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);

		try {
			StringReader stringReader = new StringReader(content);
			BufferedReader bufferedReader = new BufferedReader(stringReader);
			totalLineCount = 1;
			while (bufferedReader.readLine() != null) {
				totalLineCount++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<Comment> commentList = compilationUnit.getCommentList();
		for (Comment comment : commentList) {
			commentLineCount += ASTHelper.calcLineCount(compilationUnit, comment);
		}

		List<AbstractTypeDeclaration> types = compilationUnit.types();

		for (AbstractTypeDeclaration type : types) {
			if (type instanceof TypeDeclaration) {

				totalLineCount += ASTHelper.calcLineCount(compilationUnit, compilationUnit);

				TypeDeclaration typeDeclaration = (TypeDeclaration) type;

				FieldDeclaration[] fieldDeclarations = typeDeclaration.getFields();
				for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
					List<VariableDeclarationFragment> fragments = fieldDeclaration.fragments();
					fieldCount += fragments.size();
				}

				MethodDeclaration[] methodDeclarations = typeDeclaration.getMethods();
				methodCount += methodDeclarations.length;

				for (MethodDeclaration methodDeclaration : methodDeclarations) {
					McCCVistor mcCCVistor = new McCCVistor();
					methodDeclaration.accept(mcCCVistor);
					if (mcCCVistor.getMcCC() > maxCoc) {
						maxCoc = mcCCVistor.getMcCC();
					}
				}

			}
		}

		Metric result = new Metric();
		result.setTotalLineCount(totalLineCount);
		result.setCommentLineCount(commentLineCount);
		result.setFieldCount(fieldCount);
		result.setMethodCount(methodCount);
		result.setMaxCoc(maxCoc);

		return result;
	}

}
