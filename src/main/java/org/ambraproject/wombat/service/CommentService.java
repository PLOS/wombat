package org.ambraproject.wombat.service;

import java.io.IOException;
import java.util.Map;

public interface CommentService {

  Map<String, Object> getComment(String commentId) throws IOException;

}
