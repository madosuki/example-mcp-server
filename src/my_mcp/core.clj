(ns my-mcp.core
  (:import (io.modelcontextprotocol.server McpServer)
           (io.modelcontextprotocol.spec McpSchema)
           (io.modelcontextprotocol.spec McpSchema$ServerCapabilities)
           (io.modelcontextprotocol.server.transport StdioServerTransportProvider)
           (io.modelcontextprotocol.server.transport HttpServletSseServerTransportProvider)
           (com.fasterxml.jackson.databind ObjectMapper)))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn main []
  (foo 'neko)
  (let [server-capabilities (.build (.prompts (McpSchema$ServerCapabilities/builder) true))
        json-mapper (new ObjectMapper)        
        transport (new HttpServletSseServerTransportProvider json-mapper "/mcp/message" "/mcp/sse")
        server (.build
                (.capabilities
                 (.serverInfo
                  (McpServer/sync transport)
                  "localhost"
                  "0.0.1")
                 server-capabilities))
        ]
    ))
