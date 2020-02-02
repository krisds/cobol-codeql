package com.semmle.cobol.util;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import koopa.core.util.JVM;

/**
 * Reads some logging, and formats it as a JTree.
 */
public class VisualTrace {

	private Stack<DefaultMutableTreeNode> nodes = new Stack<DefaultMutableTreeNode>();
	private JFrame frame = null;

	public VisualTrace() {
	}

	private void ensureFrameIsVisible() {
		if (frame != null)
			return;

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Visual Trace");
		nodes.push(root);

		JTree tree = new JTree(root);

		JScrollPane treeView = new JScrollPane(tree);

		frame = new JFrame("Visual Trace");
		frame.add(treeView);
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				JVM.enableSystemExitCall();
			}
		});

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		frame.setVisible(true);
	}

	private void add(String message) {
		ensureFrameIsVisible();
		nodes.peek().add(new DefaultMutableTreeNode(message));
	}

	private void indent(String message) {
		ensureFrameIsVisible();
		final DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(
				message);
		nodes.peek().add(newChild);
		nodes.push(newChild);
	}

	private void dedent(String message) {
		ensureFrameIsVisible();
		nodes.pop().setUserObject(message);
		// nodes.peek().add(new DefaultMutableTreeNode(message));
	}

	private void load(String filename) throws IOException {
		File f = new File(filename);
		FileReader r = new FileReader(f);
		BufferedReader b = new BufferedReader(r);

		String line = null;
		while ((line = b.readLine()) != null) {
			if (line.length() == 0)
				continue;
			else if (line.startsWith("> "))
				indent(line.substring(2));
			else if (line.startsWith("< "))
				dedent(line.substring(2));
			else if (line.startsWith(". "))
				add(line.substring(2));
			else
				add(line);
		}

		b.close();
	}

	public static void main(String[] args) throws IOException {
		VisualTrace visual = new VisualTrace();

		visual.load("testsuite/TEST.trace");

		visual.ensureFrameIsVisible();
	}
}
