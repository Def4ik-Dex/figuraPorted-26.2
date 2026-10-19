package org.figuramc.figura.lua.api.net;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.ReadOnlyLuaTable;
import org.figuramc.figura.lua.api.data.FiguraBuffer;
import org.figuramc.figura.lua.api.data.FiguraFuture;
import org.figuramc.figura.lua.api.data.FiguraInputStream;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(name = "HttpAPI", value = "http")
public class HttpRequestsAPI {
   private final NetworkingAPI parent;
   private final HttpClient httpClient;
   private static final List<String> disallowedHeaders = Arrays.asList("Host", "X-Forwarded-Host", "X-Host");

   HttpRequestsAPI(NetworkingAPI parent) {
      this.parent = parent;
      this.httpClient = HttpClient.newBuilder().build();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "http.request",
      overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "uri", returnType = HttpRequestsAPI.HttpRequestBuilder.class)
   )
   public HttpRequestsAPI.HttpRequestBuilder request(@LuaNotNil String uri) {
      return new HttpRequestsAPI.HttpRequestBuilder(this, uri);
   }

   @Override
   public String toString() {
      return "HttpAPI";
   }

   @LuaWhitelist
   @LuaTypeDoc(name = "HttpRequestBuilder", value = "http_request_builder")
   public static class HttpRequestBuilder {
      private final HttpRequestsAPI parent;
      private String uri;
      private String method = "GET";
      private Object data;
      private final HashMap<String, String> headers = new HashMap<>();

      public HttpRequestBuilder(HttpRequestsAPI parent, String uri) {
         this.parent = parent;
         this.uri = uri;
      }

      @LuaWhitelist
      @LuaMethodDoc(
         value = "http_request_builder.uri",
         overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "uri", returnType = HttpRequestsAPI.HttpRequestBuilder.class)
      )
      public HttpRequestsAPI.HttpRequestBuilder uri(@LuaNotNil String uri) {
         this.uri = uri;
         return this;
      }

      @LuaWhitelist
      @LuaMethodDoc(
         value = "http_request_builder.method",
         overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "method", returnType = HttpRequestsAPI.HttpRequestBuilder.class)
      )
      public HttpRequestsAPI.HttpRequestBuilder method(String method) {
         this.method = Objects.requireNonNullElse(method, "GET");
         return this;
      }

      @LuaWhitelist
      @LuaMethodDoc(
         value = "http_request_builder.body",
         overloads = {
               @LuaMethodOverload(argumentTypes = FiguraInputStream.class, argumentNames = "data", returnType = HttpRequestsAPI.HttpRequestBuilder.class),
               @LuaMethodOverload(argumentTypes = FiguraBuffer.class, argumentNames = "data", returnType = HttpRequestsAPI.HttpRequestBuilder.class)
         }
      )
      public HttpRequestsAPI.HttpRequestBuilder body(Object data) {
         if (data != null && !(data instanceof InputStream) && !(data instanceof FiguraBuffer)) {
            throw new LuaError("Invalid request body type");
         }

         this.data = data;
         return this;
      }

      @LuaWhitelist
      @LuaMethodDoc(
         value = "http_request_builder.header",
         overloads = @LuaMethodOverload(
            argumentNames = {"header", "value"},
            argumentTypes = {String.class, String.class},
            returnType = HttpRequestsAPI.HttpRequestBuilder.class
         )
      )
      public HttpRequestsAPI.HttpRequestBuilder header(@LuaNotNil String header, String value) {
         if (value == null) {
            this.headers.remove(header);
         } else {
            this.headers.put(header, value);
         }

         return this;
      }

      @LuaWhitelist
      @LuaMethodDoc(value = "http_request_builder.get_uri", overloads = @LuaMethodOverload(returnType = String.class))
      public String getUri() {
         return this.uri;
      }

      @LuaWhitelist
      @LuaMethodDoc(value = "http_request_builder.get_method", overloads = @LuaMethodOverload(returnType = String.class))
      public String getMethod() {
         return this.method;
      }

      @LuaWhitelist
      @LuaMethodDoc(value = "http_request_builder.get_data", overloads = @LuaMethodOverload(returnType = Objects.class))
      public Object getBody() {
         return this.data;
      }

      @LuaWhitelist
      @LuaMethodDoc(value = "http_request_builder.get_headers", overloads = @LuaMethodOverload(returnType = LuaTable.class))
      public HashMap<String, String> getHeaders() {
         return this.headers;
      }

      private InputStream inputStreamSupplier() {
         return this.data instanceof InputStream is ? is : ((FiguraBuffer)this.data).asInputStream();
      }

      private HttpRequest getRequest() {
         Builder builder = HttpRequest.newBuilder(URI.create(this.uri));
         BodyPublisher bp = this.data != null ? BodyPublishers.ofInputStream(this::inputStreamSupplier) : BodyPublishers.noBody();

         for (Entry<String, String> entry : this.getHeaders().entrySet()) {
            if (HttpRequestsAPI.disallowedHeaders.stream().anyMatch(s -> s.equalsIgnoreCase(entry.getKey()))) {
               if (this.parent.parent.owner.isHost) {
                  FiguraMod.sendChatMessage(Component.translatable("figura.network.header_disabled", new Object[]{entry.getKey()}));
               }
            } else {
               builder.header(entry.getKey(), entry.getValue());
            }
         }

         builder.method(this.getMethod(), bp);
         return builder.build();
      }

      @LuaWhitelist
      @LuaMethodDoc("http_request_builder.send")
      public FiguraFuture<HttpRequestsAPI.HttpResponse> send() {
         String uri = this.getUri();

         try {
            this.parent.parent.securityCheck(uri);
         } catch (NetworkingAPI.LinkNotAllowedException e) {
            this.parent
               .parent
               .error(NetworkingAPI.LogSource.HTTP, Component.literal("Tried to send %s request to not allowed link %s".formatted(this.method, uri)));
            throw e.luaError;
         }

         this.parent.parent.log(NetworkingAPI.LogSource.HTTP, Component.literal("Sent %s request to %s".formatted(this.method, uri)));
         HttpRequest req = this.getRequest();
         FiguraFuture<HttpRequestsAPI.HttpResponse> future = new FiguraFuture<>();
         CompletableFuture<java.net.http.HttpResponse<InputStream>> asyncResponse = this.parent.httpClient.sendAsync(req, BodyHandlers.ofInputStream());
         asyncResponse.whenCompleteAsync(
            (response, t) -> {
               if (t != null) {
                  future.error(t);
               } else {
                  future.complete(
                     new HttpRequestsAPI.HttpResponse(
                        new FiguraInputStream(this.parent.parent.owner, response.body()), response.statusCode(), response.headers().map()
                     )
                  );
               }
            }
         );
         return future;
      }

      @Override
      public String toString() {
         return "HttpRequestBuilder(%s:%s)".formatted(this.method, this.uri);
      }
   }

   @LuaWhitelist
   @LuaTypeDoc(name = "HttpResponse", value = "http_response")
   public static class HttpResponse {
      private final FiguraInputStream data;
      private final int responseCode;
      private final ReadOnlyLuaTable headersTable;

      public HttpResponse(FiguraInputStream data, int responseCode, Map<String, List<String>> headers) {
         this.data = data;
         this.responseCode = responseCode;
         LuaTable headersTable = new LuaTable();

         for (Entry<String, List<String>> entry : headers.entrySet()) {
            LuaTable headerTable = new LuaTable();

            for (String headerVal : entry.getValue()) {
               headerTable.set(headerTable.length() + 1, LuaValue.valueOf(headerVal));
            }

            headersTable.set(entry.getKey(), new ReadOnlyLuaTable(headerTable));
         }

         this.headersTable = new ReadOnlyLuaTable(headersTable);
      }

      @LuaWhitelist
      @LuaMethodDoc(value = "http_response.get_data", overloads = @LuaMethodOverload(returnType = FiguraInputStream.class))
      public FiguraInputStream getData() {
         return this.data;
      }

      @LuaWhitelist
      @LuaMethodDoc(value = "http_response.get_response_code", overloads = @LuaMethodOverload(returnType = int.class))
      public int getResponseCode() {
         return this.responseCode;
      }

      @LuaWhitelist
      @LuaMethodDoc(value = "http_response.get_headers", overloads = @LuaMethodOverload(returnType = ReadOnlyLuaTable.class))
      public ReadOnlyLuaTable getHeaders() {
         return this.headersTable;
      }

      @Override
      public String toString() {
         return "HttpResponse(%s)".formatted(this.responseCode);
      }
   }
}
