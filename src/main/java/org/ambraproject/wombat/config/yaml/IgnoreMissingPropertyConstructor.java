package org.ambraproject.wombat.config.yaml;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * Class to support properties in the YAML file without a corresponding JavaBean.
 *
 * The default Snake YAML implementation will throw an exception, which will prevent the
 * application from starting.
 *
 * However, this class will ignore the extra property in the YAML file, and allows the
 * application to start-up.
 */
public class IgnoreMissingPropertyConstructor extends Constructor {

  private static final Logger LOG = LoggerFactory.getLogger(IgnoreMissingPropertyConstructor.class);

  /**
   * This is the class the handles the association between the YAML properties and a
   * corresponding JavaBean.
   *
   * Note, this is a clone of
   * {@link org.yaml.snakeyaml.constructor.Constructor.ConstructMapping Constructor.ConstructMapping},
   * except that it will ignore missing YAML property to JavaBean association.
   */
  protected class IgnoreMissingPropertyConstructMapping extends Constructor.ConstructMapping {

    @Override
    protected Object constructJavaBean2ndStep(MappingNode node, Object object) {
      flattenMapping(node);
      final Class<? extends Object> beanType = node.getType();
      final List<NodeTuple> nodeValue = node.getValue();
      for (NodeTuple tuple : nodeValue) {
        ScalarNode keyNode;
        if (tuple.getKeyNode() instanceof ScalarNode) {
          // key must be scalar
          keyNode = (ScalarNode) tuple.getKeyNode();
        } else {
          throw new YAMLException("Keys must be scalars but found: " + tuple.getKeyNode());
        }
        final Node valueNode = tuple.getValueNode();
        // keys can only be Strings
        keyNode.setType(String.class);
        final String key = (String) constructObject(keyNode);
        try {
          final Property property = getProperty(beanType, key);
          valueNode.setType(property.getType());
          final TypeDescription memberDescription = typeDefinitions.get(beanType);
          boolean typeDetected = false;
          if (memberDescription != null) {
            switch (valueNode.getNodeId()) {
              case sequence:
                final SequenceNode snode = (SequenceNode) valueNode;
                final Class<? extends Object> memberType =
                    memberDescription.getListPropertyType(key);
                if (memberType != null) {
                  snode.setListType(memberType);
                  typeDetected = true;
                } else if (property.getType().isArray()) {
                  snode.setListType(property.getType().getComponentType());
                  typeDetected = true;
                }
                break;
              case mapping:
                final MappingNode mnode = (MappingNode) valueNode;
                final Class<? extends Object> keyType = memberDescription.getMapKeyType(key);
                if (keyType != null) {
                  mnode.setTypes(keyType, memberDescription.getMapValueType(key));
                  typeDetected = true;
                }
                break;
              default: // scalar
            }
          }
          if (!typeDetected && valueNode.getNodeId() != NodeId.scalar) {
            // only if there is no explicit TypeDescription
            final Class<?>[] arguments = property.getActualTypeArguments();
            if (arguments != null && arguments.length > 0) {
              // type safe (generic) collection may contain the
              // proper class
              if (valueNode.getNodeId() == NodeId.sequence) {
                final Class<?> t = arguments[0];
                final SequenceNode snode = (SequenceNode) valueNode;
                snode.setListType(t);
              } else if (valueNode.getTag().equals(Tag.SET)) {
                final Class<?> t = arguments[0];
                final MappingNode mnode = (MappingNode) valueNode;
                mnode.setOnlyKeyType(t);
                mnode.setUseClassConstructor(true);
              } else if (property.getType().isAssignableFrom(Map.class)) {
                final Class<?> ketType = arguments[0];
                final Class<?> valueType = arguments[1];
                final MappingNode mnode = (MappingNode) valueNode;
                mnode.setTypes(ketType, valueType);
                mnode.setUseClassConstructor(true);
              } else {
                // the type for collection entries cannot be
                // detected.
              }
            }
          }
          final Object value = constructObject(valueNode);
          property.set(object, value);
        } catch (Exception exception) {
          if (exception != null && exception instanceof YAMLException) {
            final String message = exception.getMessage();
            if (message.contains("Unable to find property ")) {
              // Ignore the missing JavaBean to store the property, and continue
              // parsing the YAML.
              LOG.warn("Caught exception while parsing YAML: {}", exception);
              continue;
            }
          }

          throw new RuntimeException("Cannot create property=" + key + " for JavaBean=" + object,
              exception);
        }
      }
      return object;
    }

  }

  /**
   * Creates a <code>IgnoreMissingPropertyConstructor</code> instance.
   */
  public IgnoreMissingPropertyConstructor() {
    super();
    yamlClassConstructors.put(NodeId.mapping, new IgnoreMissingPropertyConstructMapping());
  }
}
