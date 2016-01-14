/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.yaml;

import org.springframework.ide.eclipse.editor.support.completions.ICompletionEngine;
import org.springframework.ide.eclipse.editor.support.util.YamlIndentUtil;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPath;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SKeyNode;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SNodeType;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SSeqNode;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

/**
 * Abstratc superclass to make it easier to define {@link ICompletionEngine} implementation
 * for .yml file.
 *
 * @author Kris De Volder
 */
public abstract class YamlCompletionEngine implements ICompletionEngine {

	public YamlCompletionEngine(YamlStructureProvider structureProvider) {
		this.structureProvider= structureProvider;
	}

	protected YamlStructureProvider structureProvider;

	protected YamlPath getContextPath(YamlDocument doc, SNode node, int offset) throws Exception {
		if (node==null) {
			return YamlPath.EMPTY;
		} else if (node.getNodeType()==SNodeType.KEY) {
			//slight complication. The area in the key and value of a key node represent different
			// contexts for content assistance
			SKeyNode keyNode = (SKeyNode)node;
			if (keyNode.isInValue(offset)) {
				return keyNode.getPath();
			} else {
				return keyNode.getParent().getPath();
			}
		} else if (node.getNodeType()==SNodeType.RAW) {
			//Treat raw node as a 'key node'. This is basically assuming that is misclasified
			// by structure parser because the ':' was not yet typed into the document.
	
			//Complication: if line with cursor is empty or the cursor is inside the indentation
			// area then the structure may not reflect correctly the context. This is because
			// the correct context depends on text the user has not typed yet.(which will change the
			// indentation level of the current line. So we must use the cursorIndentation
			// rather than the structur-tree to determine the 'context' node.
			int cursorIndent = doc.getColumn(offset);
			int nodeIndent = node.getIndent();
			int currentIndent = YamlIndentUtil.minIndent(cursorIndent, nodeIndent);
			while (node.getIndent()==-1 || (node.getIndent()>=currentIndent && node.getNodeType()!=SNodeType.DOC)) {
				node = node.getParent();
			}
			return node.getPath();
		} else if (node.getNodeType()==SNodeType.SEQ) {
			SSeqNode seqNode = (SSeqNode)node;
			if (seqNode.isInValue(offset)) {
				return seqNode.getPath();
			} else {
				return seqNode.getParent().getPath();
			}
		} else if (node.getNodeType()==SNodeType.DOC) {
			return node.getPath();
		} else {
			throw new IllegalStateException("Missing case");
		}
	}

}