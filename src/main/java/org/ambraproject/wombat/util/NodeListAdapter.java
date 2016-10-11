/*
 * Copyright (c) 2006-2012 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.util;

import com.google.common.base.Preconditions;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

/**
 * Adapt a collection from the W3C DOM library to be a {@link java.util.List} compatible with the Java Collections Framework.
 */
public abstract class NodeListAdapter extends AbstractList<Node> {

  /**
   * Wrap a node list. The returned list is an unmodifiable view and supports all non-destructive operations. Any
   * changes in the underlying list will be reflected in this object.
   *
   * @param nodes a DOM node list
   * @return a Java Collections Framework node list
   * @throws NullPointerException if {@code nodes} is null
   */
  public static List<Node> wrap(NodeList nodes) {
    return new NodeListWrapper(nodes);
  }

  /**
   * Wrap a node map. The returned list is an unmodifiable view and supports all non-destructive operations. Any changes
   * in the underlying map will be reflected in this object.
   *
   * @param nodes a DOM node map
   * @return a Java Collections Framework node list
   * @throws NullPointerException if {@code nodes} is null
   */
  public static List<Node> wrap(NamedNodeMap nodes) {
    return new NodeMapWrapper(nodes);
  }


  abstract Node getImpl(int index);

  /**
   * {@inheritDoc}
   */
  @Override
  public Node get(int index) {
    // To match the List contract (nodes.item returns null on invalid index)
    Preconditions.checkElementIndex(index, size());

    Node node = getImpl(index);
    if (node == null) {
      throw new NullPointerException("Delegate object returned null for a valid index");
    }
    return node;
  }

  /*
   * Nested subclasses only.
   */
  private NodeListAdapter() {
  }

  private static class NodeListWrapper extends NodeListAdapter {
    private final NodeList wrapped;

    private NodeListWrapper(NodeList wrapped) {
      this.wrapped = Preconditions.checkNotNull(wrapped);
    }

    @Override
    protected Node getImpl(int index) {
      return wrapped.item(index);
    }

    @Override
    public int size() {
      return wrapped.getLength();
    }
  }

  private static class NodeMapWrapper extends NodeListAdapter {
    private final NamedNodeMap wrapped;

    private NodeMapWrapper(NamedNodeMap wrapped) {
      this.wrapped = Preconditions.checkNotNull(wrapped);
    }

    @Override
    protected Node getImpl(int index) {
      return wrapped.item(index);
    }

    @Override
    public int size() {
      return wrapped.getLength();
    }
  }


  // Fail fast on destructive operations

  /**
   * @deprecated Unmodifiable
   */
  @Deprecated
  @Override
  public boolean add(Node node) {
    throw new UnsupportedOperationException("Unmodifiable");
  }

  /**
   * @deprecated Unmodifiable
   */
  @Deprecated
  @Override
  public void add(int index, Node element) {
    throw new UnsupportedOperationException("Unmodifiable");
  }

  /**
   * @deprecated Unmodifiable
   */
  @Deprecated
  @Override
  public boolean addAll(int index, Collection<? extends Node> c) {
    throw new UnsupportedOperationException("Unmodifiable");
  }

  /**
   * @deprecated Unmodifiable
   */
  @Deprecated
  @Override
  public void clear() {
    throw new UnsupportedOperationException("Unmodifiable");
  }

  /**
   * @deprecated Unmodifiable
   */
  @Deprecated
  @Override
  public Node remove(int index) {
    throw new UnsupportedOperationException("Unmodifiable");
  }

  /**
   * @deprecated Unmodifiable
   */
  @Deprecated
  @Override
  protected void removeRange(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException("Unmodifiable");
  }

  /**
   * @deprecated Unmodifiable
   */
  @Deprecated
  @Override
  public Node set(int index, Node element) {
    throw new UnsupportedOperationException("Unmodifiable");
  }

  /**
   * @deprecated Unmodifiable
   */
  @Deprecated
  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException("Unmodifiable");
  }

  /**
   * @deprecated Unmodifiable
   */
  @Deprecated
  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException("Unmodifiable");
  }

  /**
   * @deprecated Unmodifiable
   */
  @Deprecated
  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException("Unmodifiable");
  }

}
