package com.blinkbox.books.elasticsearch.client;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.*;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.support.AbstractClient;
import org.elasticsearch.client.support.AbstractClusterAdminClient;
import org.elasticsearch.client.support.AbstractIndicesAdminClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;

public class HttpClient extends AbstractClient {

    private Settings settings;
    private HttpTransport transport = new HttpTransport();

    public HttpClient(Settings settings) {
        this.settings = settings;
    }

    class HttpClusterAdminClient extends AbstractClusterAdminClient {

        @Override
        public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder, ClusterAdminClient>> ActionFuture<Response> execute(Action<Request, Response, RequestBuilder, ClusterAdminClient> action, Request request) {


            // This is just an example to see that types play well from java to scala
            Action<GetRequest, GetResponse, GetRequestBuilder, Client> act = null;
            GetRequest req = null;
            final ActionFuture<GetResponse> doAction = transport.doAction(act, req);

            return null;
        }

        @Override
        public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder, ClusterAdminClient>> void execute(Action<Request, Response, RequestBuilder, ClusterAdminClient> action, Request request, ActionListener<Response> listener) {

        }

        @Override
        public ThreadPool threadPool() {
            return null;
        }
    }

    class HttpIndicesAdminClient extends AbstractIndicesAdminClient {

        @Override
        public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder, IndicesAdminClient>> ActionFuture<Response> execute(Action<Request, Response, RequestBuilder, IndicesAdminClient> action, Request request) {
            return null;
        }

        @Override
        public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder, IndicesAdminClient>> void execute(Action<Request, Response, RequestBuilder, IndicesAdminClient> action, Request request, ActionListener<Response> listener) {

        }

        @Override
        public ThreadPool threadPool() {
            return null;
        }
    }

    class HttpAdminClient implements AdminClient {

        @Override
        public ClusterAdminClient cluster() {
            return new HttpClusterAdminClient();
        }

        @Override
        public IndicesAdminClient indices() {
            return new HttpIndicesAdminClient();
        }
    }

    @Override
    public AdminClient admin() {
        return new HttpAdminClient();
    }

    @Override
    public Settings settings() {
        return settings;
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder, Client>>
            ActionFuture<Response> execute(Action<Request, Response, RequestBuilder, Client> action, Request request) {
        return null;
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder, Client>> void execute(Action<Request, Response, RequestBuilder, Client> action, Request request, ActionListener<Response> listener) {

    }

    @Override
    public ThreadPool threadPool() {
        return null;
    }

    @Override
    public void close() throws ElasticsearchException {

    }
}
