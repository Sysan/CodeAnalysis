package edu.nju.tools.jdt;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * 抽象语法树（AST）相关的工具类
 * 
 * @author SuZiquan
 *
 */
public class ASTHelper {

	/**
	 * 将int表示的多个修饰符转化为String类型。
	 * 
	 * @param modifiers
	 *            ASTParser使用int表示的修饰符
	 * @return
	 */
	public static List<String> resolveModifier(int modifiers) {

		List<String> modifierList = new ArrayList<>();

		String[] modifierArray = { "public", "protected", "private", "static", "abstract", "final", "native",
				"synchronized", "transient", "volatile", "strictfp", "default" };

		try {
			Class<?> modifierClazz = Class.forName("org.eclipse.jdt.core.dom.Modifier");

			for (String mod : modifierArray) {
				char[] array = mod.toCharArray();
				array[0] -= 32;
				Method isMethod = modifierClazz.getDeclaredMethod("is" + String.valueOf(array), int.class);
				boolean isMod = (boolean) isMethod.invoke(modifierClazz, modifiers);
				if (isMod)
					modifierList.add(mod);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return modifierList;
	}

	/**
	 * 计算抽象语法树的某个节点（可能代表类、方法、字段等）的长度
	 * 
	 * @param compilationUnit
	 *            编译单元，通常是一个Java文件
	 * @param node
	 *            抽象语法树的节点（可能代表类、方法、字段等）
	 * @return
	 */
	public static int calcLineCount(CompilationUnit compilationUnit, ASTNode node) {
		int startLineNumber = compilationUnit.getLineNumber(node.getStartPosition());
		int endLineNumber = compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
		return endLineNumber - startLineNumber + 1;
	}

}
