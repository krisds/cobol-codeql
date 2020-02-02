package com.semmle.cobol.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import koopa.core.data.Data;
import koopa.core.data.Token;
import koopa.core.data.markers.Start;
import koopa.core.trees.Tree;
import koopa.core.trees.jaxen.Jaxen;

public class Util {

	@SuppressWarnings("unchecked")
	public static List<Tree> matching(final Tree root, String xpath) {
		xpath = Util.getXPath(xpath);

		List<Tree> matches = (List<Tree>) Jaxen.getMatches(root, xpath);
		if (matches != null)
			return matches;
		else
			return Collections.emptyList();
	}

	public static Tree uniqueMatch(final Tree root, String xpath) {
		List<Tree> matches = matching(root, xpath);
		if (matches != null && matches.size() == 1)
			return matches.get(0);
		else
			return null;
	}

	public static String getNodeName(Tree node) {
		Data data = node.getData();
		if (data == null || !(data instanceof Start))
			return null;

		Start start = (Start) data;
		String nodeName = start.getName();
		return nodeName;
	}

	public static Tree closest(Tree node, String... nodeNames) {
		Tree ancestor = node.getParent();

		while (ancestor != null) {
			String ancestorNodeName = getNodeName(ancestor);

			for (String nodeName : nodeNames)
				if (nodeName.equals(ancestorNodeName))
					return ancestor;

			ancestor = ancestor.getParent();
		}

		return null;
	}

	public static Tree closest(Tree node, Collection<Data> expected) {
		Tree ancestor = node.getParent();

		while (ancestor != null) {
			Data d = ancestor.getData();

			for (Data e : expected)
				if (e.equals(d))
					return ancestor;

			ancestor = ancestor.getParent();
		}

		return null;
	}

	public static List<Tree> children(Tree node, String... nodeNames) {
		List<Tree> children = new LinkedList<Tree>();

		for (Tree child : node.getChildren()) {
			String childNodeName = getNodeName(child);

			if (childNodeName == null)
				continue;

			for (String nodeName : nodeNames)
				if (childNodeName.equals(nodeName)) {
					children.add(child);
					break;
				}
		}

		return children;
	}

	public static List<Tree> children(Tree node, Collection<Data> expected) {
		List<Tree> children = new LinkedList<Tree>();

		for (Tree child : node.getChildren()) {
			Data d = child.getData();

			if (d == null)
				continue;

			for (Data e : expected)
				if (d.equals(e)) {
					children.add(child);
					break;
				}
		}

		return children;
	}

	/**
	 * When trapping a statement node what we really want to trap is the first
	 * real child of that node. This will be the actual statement. The
	 * "statement" nodes are just wrappers set up for easy querying.
	 * <p>
	 * This function fetches the actual child of a statement.
	 */
	public static Tree getEmbeddedStatement(Tree statement) {
		if (!"statement".equals(getNodeName(statement))
				&& !"compilerStatement".equals(getNodeName(statement)))
			return null;

		return getFirstNode(statement);
	}

	/**
	 * Returns the first child which is a node.
	 */
	public static Tree getFirstNode(Tree node) {
		for (int i = 0; i < node.getChildCount(); i++) {
			Tree child = node.getChild(i);
			if (child.getData() instanceof Start)
				return child;
		}

		return null;
	}

	/**
	 * For each character in the symbol set, count its occurrences in the text.
	 */
	public static int[] countSymbols(String text, String symbolSet) {
		int[] count = new int[symbolSet.length()];
		for (int i = 0; i < count.length; i++)
			count[i] = 0;

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			int index = symbolSet.indexOf(c);

			if (index >= 0)
				count[index] += 1;
		}
		return count;
	}

	public static int countOccurrences(String text, String sample) {
		int count = 0;

		int start = 0;
		while (true) {
			int index = text.indexOf(sample, start);

			if (index < 0)
				break;

			count += 1;
			start = index + sample.length();
		}

		return count;
	}

	/**
	 * Writing XPath expressions can, at times, start feeling like writing
	 * assembler. Say you need your parent's sibling, but only if both are of
	 * type 'section'. Here's the best I could come up with:
	 * 
	 * <pre>
	 * ..[local-name()='section']/following-sibling::*[1][local-name()='section']
	 * </pre>
	 * 
	 * To make this somewhat easier to write and read I'm allowing some
	 * syntactic sugar in XPath expressions appearing in the scripts. These are:
	 * 
	 * <ul>
	 * <li><code>{:next}</code> - equivalent to
	 * <code>following-sibling::*[1]</code></li>
	 * <li><code>{=name}</code> - equivalent to
	 * <code>[local-name()='name']</code>; with name being any legal node
	 * name</li>
	 * <li><code>{#name}</code> - equivalent to <code>local-name()='name'</code>
	 * ; with name being any legal node name</li>
	 * </ul>
	 * 
	 * With that, I can now rewrite the earlier XPath assembly as:
	 * 
	 * <pre>
	 * ..{=section}/{:next}{=section}
	 * </pre>
	 * 
	 * This function takes any string and will expand the syntactic sugar as
	 * described. It's not very smart, doing nothing more than simple
	 * <code>s/sugar/xpath/g</code>, so be careful.
	 */
	public static String getXPath(String text) {
		text = text.trim();

		text = text.replaceAll("\\{:next\\}", "following-sibling::*[1]");
		text = text.replaceAll("\\{=([-\\w]+)\\}", "[local-name()='$1']");
		text = text.replaceAll("\\{#([-\\w]+)\\}", "local-name()='$1'");

		return text;
	}

	/**
	 * Searches for the first token in the tree, but will skip any branch in the
	 * "semmle" namespace.
	 */
	public static Token getStartToken(Tree node) {
		if (node.isToken())
			return (Token) node.getData();

		if (node.isNode()) {
			if ("semmle".equals(((Start) node.getData()).getNamespace()))
				return null;

			for (int i = 0; i < node.getChildCount(); i++) {
				final Token token = getStartToken(node.getChild(i));
				if (token != null)
					return token;
			}
		}

		return null;
	}

	/**
	 * Searches for the last token in the tree, but will skip any branch in the
	 * "semmle" namespace.
	 */
	public static Token getEndToken(Tree node) {
		if (node.isToken())
			return (Token) node.getData();

		if (node.isNode()) {
			if ("semmle".equals(((Start) node.getData()).getNamespace()))
				return null;

			for (int i = node.getChildCount() - 1; i >= 0; i--) {
				final Token token = getEndToken(node.getChild(i));

				if (token != null)
					return token;
			}
		}

		return null;
	}
}
