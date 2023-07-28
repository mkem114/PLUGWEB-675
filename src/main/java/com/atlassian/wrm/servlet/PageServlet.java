package com.atlassian.wrm.servlet;

import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.webresource.api.assembler.WebResourceAssembler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static com.atlassian.webresource.api.UrlMode.AUTO;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class PageServlet extends HttpServlet {
    public static final String WRM_CLIENTSIDE_WEBRESOURCE_KEY = "com.atlassian.plugins.atlassian-plugins-webresource-rest:web-resource-manager";
    private final PageBuilderService pageBuilderService;
    private final TemplateRenderer templateRenderer;

    public PageServlet(final PageBuilderService pageBuilderService, final TemplateRenderer templateRenderer) {
        this.pageBuilderService = requireNonNull(pageBuilderService, "pageBuilderService");
        this.templateRenderer = requireNonNull(templateRenderer, "templateRenderer");
    }

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        // Prepare the WRM clientside API
        final WebResourceAssembler webResourceAssembler = pageBuilderService.assembler();
        webResourceAssembler.resources().requireWebResource(WRM_CLIENTSIDE_WEBRESOURCE_KEY);
        if (!isNull(req.getQueryString()) && req.getQueryString().contains("2")) {
            // This dependency will require the same resources again, but from JS
            webResourceAssembler.resources().requireContext("requires-executed-twice");

            // Perhaps some other plugin, maybe a macro or the product uses the WRM again in the body
            webResourceAssembler.resources().requireContext("some-context");

            // Basic case of only writing the tags to the HTML once
            webResourceAssembler.assembled().drainIncludedResources().writeHtmlTags(resp.getWriter(), AUTO);
        } else {
            // Imagine some products drain and write their tags in the <head>
            webResourceAssembler.assembled().drainIncludedResources().writeHtmlTags(resp.getWriter(), AUTO);

            // Then some plugin comes along and renders its template or REQUIRE or INLNIE phases its JS
            // Render some JS that races the WRM clientside against the server-side rendered included resources
            templateRenderer.render("vm/example.vm", Collections.emptyMap(), resp.getWriter());

            // Perhaps some other plugin, maybe a macro or the product uses the WRM again in the body
            // This context will include the same resources again
            webResourceAssembler.resources().requireContext("some-context");
            webResourceAssembler.assembled().drainIncludedResources().writeHtmlTags(resp.getWriter(), AUTO);
        }
    }
}
