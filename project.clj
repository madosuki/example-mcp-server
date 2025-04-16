(defproject my-mcp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :repositories [["sonatype" {:url "https://oss.sonatype.org/content/repositories/releases" :update :always}]]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [io.modelcontextprotocol.sdk/mcp "0.9.0"]
                 [jakarta.servlet/jakarta.servlet-api "6.1.0"]
                 [org.eclipse.jetty/jetty-server "12.0.19"]
                 [org.eclipse.jetty.ee10/jetty-ee10-servlet "12.0.19"]]
  :repl-options {:init-ns my-mcp.core}
  :profiles {:uberjar {:aot :all}}
  :aot [my-mcp.core]
  :main my-mcp.core)
