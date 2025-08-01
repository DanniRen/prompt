package com.rdn.prompt.constants;

public class PromptConstants {
    public static final String PROMPT_MAPPING = """
            {
              "mappings": {
                "properties": {
                  "id": {
                    "type": "keyword"
                  },
                  "title": {
                    "type": "text",
                    "analyzer": "ik_max_word"
                  },
                  "description": {
                    "type": "text",
                    "analyzer": "ik_max_word"
                  },
                  "content": {
                    "type": "text",
                    "analyzer": "ik_max_word"
                  },
                  "tags": {
                    "type": "text",
                    "analyzer": "ik_max_word"
                  },
                  "scene": {
                    "type": "text",
                    "analyzer": "ik_max_word"
                  }
                }
              }
            }
            """;
    
}
