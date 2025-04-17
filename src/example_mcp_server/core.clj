(ns example-mcp-server.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:import (java.util.function BiFunction)
           (io.modelcontextprotocol.server McpServer)
           (io.modelcontextprotocol.server McpServerFeatures)
           (io.modelcontextprotocol.server McpServerFeatures$SyncPromptSpecification)
           (io.modelcontextprotocol.spec McpSchema)
           (io.modelcontextprotocol.spec McpSchema$Prompt)
           (io.modelcontextprotocol.spec McpSchema$GetPromptResult)
           (io.modelcontextprotocol.spec McpSchema$PromptArgument)
           (io.modelcontextprotocol.spec McpSchema$PromptMessage)
           (io.modelcontextprotocol.spec McpSchema$Role)
           (io.modelcontextprotocol.spec McpSchema$TextContent)
           (io.modelcontextprotocol.spec McpSchema$ServerCapabilities)
           (io.modelcontextprotocol.server.transport StdioServerTransportProvider)
           (io.modelcontextprotocol.server.transport HttpServletSseServerTransportProvider)
           (com.fasterxml.jackson.databind ObjectMapper)
           (org.eclipse.jetty.ee10.servlet ServletContextHandler)
           (org.eclipse.jetty.ee10.servlet ServletHolder)
           (org.eclipse.jetty.server Server)))

(defn create-java-hash-map-with-initialize [l]
  (let [r (new java.util.HashMap)]
    (for [[k v] l]
      (.put r k v))
    r))

(defn generate-prompt-arguments []
  (let [arg (McpSchema$PromptArgument. "arg1" "arg1 desc" false)
        l (java.util.ArrayList.)]
    (.add l arg)
    l))

(defn generate-prompt-result [desc msg]
  (let [l (java.util.ArrayList.)]
    (.add l msg)
    (McpSchema$GetPromptResult. desc l)))

(defn run-http-server [transport port]
  (let [servlet-context-handler (new ServletContextHandler ServletContextHandler/SESSIONS)
        servlet-holder (new ServletHolder transport)
        http-server (new Server port)]
    (.setContextPath servlet-context-handler "/")
    (.addServlet servlet-context-handler servlet-holder "/*")
    (.setHandler http-server servlet-context-handler)
    (.start http-server)))

(def cli-options [["-s" "--sse" "run sse mode"
                   :default false]
                  ["-p" "--port PORT" "set port for sse mode"
                   :default 8080
                   :parse-fn #(Integer/parseInt %)]])

(defn -main [& args]
    (let [server-capabilities (.build (.prompts
                                     (McpSchema$ServerCapabilities/builder)
                                     true))
        ;; prompts (McpSchema$Prompt. "name"
        ;;                            "description"
        ;;                            (generate-prompt-arguments))
        prompts (McpSchema$Prompt. "name"
                                   "description"
                                   (java.util.ArrayList.))
        prompts-specification (McpServerFeatures$SyncPromptSpecification.
                               prompts
                               (reify BiFunction (apply [this exchange request]
                                                   (let [content (McpSchema$TextContent. "prompt text")
                                                         msg (McpSchema$PromptMessage. McpSchema$Role/USER content)]
                                                     (generate-prompt-result "desc of prompt" msg)))))
        ;; prompts-map (create-java-hash-map-with-initialize [["key" prompts-specification]])
        {:keys [options _ _ _]} (parse-opts args cli-options)]
      (if (:sse options)
        (let [json-mapper (new ObjectMapper)        
              transport-for-http (new HttpServletSseServerTransportProvider json-mapper "/mcp/message" "/mcp/sse")
              server-name "clojure-mcp"
              server-version "0.0.1"
              mcp-server (.build
                          (.capabilities
                           (.serverInfo
                            (McpServer/sync transport-for-http)
                            server-name
                            server-version)
                           server-capabilities))]
          (.addPrompt mcp-server prompts-specification)
          (run-http-server transport-for-http (:port options)))
        (let [transport (new StdioServerTransportProvider)
              server-name "clojure-mcp"
              server-version "0.0.1"
              mcp-server (.build
                          (.capabilities
                           (.serverInfo
                            (McpServer/sync transport)
                            server-name
                            server-version)
                           server-capabilities))]
          (.addPrompt mcp-server prompts-specification)))))
