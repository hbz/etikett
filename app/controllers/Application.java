/*Copyright (c) 2015 "hbz"

This file is part of etikett.

etikett is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.Etikett;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F.Promise;
import play.mvc.Call;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import views.html.delete;
import views.html.edit;
import views.html.index;
import views.html.message;
import views.html.upload;
import views.html.editWeights;

/**
 * @author Jan Schnasse
 *
 */
@BasicAuth
public class Application extends MyController {

    /**
     * @param urlAddress
     * @return the data of the corresponding row
     */
    public static Result row(String urlAddress) {

        if (request().accepts("text/html")) {
            return allAsHtml(urlAddress);
        } else {
            return contextAsJson(urlAddress);
        }

    }

    /**
     * @param urlAddress
     *            a url address
     * @param column
     *            the name of the column
     * @return a string with the content of the column
     */
    public static Promise<Result> getColumn(String urlAddress, String column, String lang) {
        return Promise.promise(() -> {
            if (column == null) {
                return row(urlAddress);
            }
            response().setHeader("Content-Type", "text/plain; charset=utf-8");
            if (column != null && !column.isEmpty() && urlAddress != null) {
                Etikett entry = Globals.profile.findEtikett(urlAddress);
                switch (column) {
                case "Icon":
                    return ok(entry.icon);
                case "Label": {
                    if (lang == null) {
                        return ok(entry.label);
                    } else {
                        entry.setMultiLangSerialized(entry.getMultiLangSerialized());
                        if (entry.getMultilangLabel() == null) {
                            return ok(entry.label + " (No label in '" + lang + "' available.)");
                        }
                        String label = entry.getMultilangLabel().get(lang);
                        if (label != null && !label.isEmpty()) {
                            return ok(label);
                        } else {
                            return ok(entry.label + " (No label in '" + lang + "' available.)");
                        }
                    }
                }
                case "Name":
                    return ok(entry.name);
                case "Uri":
                    return ok(entry.uri);
                case "RefType":
                    return ok(entry.referenceType);
                case "Container":
                    return ok(entry.container);
                case "Comment":
                    return ok(entry.comment);
                case "Weight":
                    return ok(entry.weight);
                }
                return ok(entry.label);
            }
            return status(500);
        });

    }

    /**
     * @param urlAddress
     * @return all data as json array
     */
    public static Result asJson(String urlAddress) {
        try {
            response().setHeader("Content-Type", "application/json; charset=utf-8");
            if (urlAddress != null) {
                Etikett entry = Globals.profile.findEtikett(urlAddress);
                ArrayList<Etikett> result = new ArrayList<Etikett>();
                result.add(entry);
                return ok(json(result));
            } else {
                return ok(json(new ArrayList<Etikett>(Globals.profile.getValues())));
            }
        } catch (Exception e) {
            play.Logger.debug("", e);
            return status(500, json(e.toString()));
        }

    }

    /**
     * @param urlAddress
     * @return all data as json array
     */
    public static Result contextAsJson(String urlAddress) {
        try {
            response().setHeader("Content-Type", "application/json; charset=utf-8");
            if (urlAddress != null) {
                Etikett entry = Globals.profile.findEtikett(urlAddress);
                ArrayList<Etikett> result = new ArrayList<Etikett>();
                result.add(entry);
                return ok(json(result));
            } else {
                return ok(json(new ArrayList<Etikett>(Globals.profile.getContextValues())));
            }
        } catch (Exception e) {
            play.Logger.debug("", e);
            return status(500, json(e.toString()));
        }

    }

    /**
     * @param urlAddress
     * @return all data as json array
     */
    public static Result cacheAsJson(String urlAddress) {
        try {
            response().setHeader("Content-Type", "application/json; charset=utf-8");
            if (urlAddress != null) {
                Etikett entry = Globals.profile.findEtikett(urlAddress);
                ArrayList<Etikett> result = new ArrayList<Etikett>();
                result.add(entry);
                return ok(json(result));
            } else {
                return ok(json(new ArrayList<Etikett>(Globals.profile.getCacheValues())));
            }
        } catch (Exception e) {
            play.Logger.debug("", e);
            return status(500, json(e.toString()));
        }

    }

    public static Promise<Result> contextAsHtml() {
        return Promise.promise(() -> {
            try {
                response().setHeader("Content-Type", "text/html; charset=utf-8");
                return ok(index.render("Context entries", new ArrayList<Etikett>(Globals.profile.getContextValues())));
            } catch (Exception e) {
                play.Logger.debug("", e);
                return status(500, message.render("Server encounters internal problem: " + e.getMessage()));
            }
        });
    }

    public static Promise<Result> conceptsAsHtml() {
        return Promise.promise(() -> {
            try {
                response().setHeader("Content-Type", "text/html; charset=utf-8");
                return ok(index.render("Configured entries", new ArrayList<Etikett>(Globals.profile.getStoreValues())));
            } catch (Exception e) {
                play.Logger.debug("", e);
                return status(500, message.render("Server encounters internal problem: " + e.getMessage()));
            }
        });
    }

    public static Promise<Result> cacheAsHtml() {
        return Promise.promise(() -> {
            try {
                response().setHeader("Content-Type", "text/html; charset=utf-8");
                return ok(index.render("Cache entries", new ArrayList<Etikett>(Globals.profile.getCacheValues())));
            } catch (Exception e) {
                play.Logger.debug("", e);
                return status(500, message.render("Server encounters internal problem: " + e.getMessage()));
            }
        });
    }

    private static Result allAsHtml(String urlAddress) {
        try {
            response().setHeader("Content-Type", "text/html; charset=utf-8");
            if (urlAddress != null) {
                Etikett entry = Globals.profile.findEtikett(urlAddress);
                ArrayList<Etikett> result = new ArrayList<Etikett>();
                result.add(entry);
                return ok(index.render("Show etikett", result));
            } else {
                return redirect(routes.Application.contextAsHtml());
            }
        } catch (Exception e) {
            play.Logger.debug("", e);
            return status(500, message.render("Server encounters internal problem: " + e.getMessage()));
        }
    }

    /**
     * @param url
     *            a string to apply the urlencoding to
     * @return a url encoded string for the passed url argument
     * @throws UnsupportedEncodingException
     *             if string is not a url and not null
     */
    @SuppressWarnings("javadoc")
    public static String EncodeURL(final String url) throws java.io.UnsupportedEncodingException {
        if (url == null)
            return "null";
        return java.net.URLEncoder.encode(url, "UTF-8");
    }

    /**
     * @param call
     *            a play call to encode
     * @return the encoded call
     * @throws UnsupportedEncodingException
     *             if string is not a url
     */
    @SuppressWarnings("javadoc")
    public static String EncodeURL(Call call) throws java.io.UnsupportedEncodingException {
        return EncodeURL(call.toString());
    }

    /**
     * @return http status
     */
    @SuppressWarnings("unchecked")
    @BasicAuth
    public static Promise<Result> addData() {
        return new ModifyAction().call(() -> {
            try {
                MultipartFormData body = request().body().asMultipartFormData();
                DynamicForm requestData = Form.form().bindFromRequest();
                String format = requestData.get("format-cb");
                String language = requestData.get("lang");
                play.Logger.debug(format);
                FilePart data = body.getFile("data");
                if (data != null) {
                    File file = data.getFile();
                    try (FileInputStream uploadData = new FileInputStream(file)) {
                        if ("Rdf-Turtle".equals(format)) {
                            Globals.profile.addRdfData(uploadData, language);
                        } else if ("Json".equals(format)) {
                            Globals.profile.addJsonData((List<Etikett>) new ObjectMapper().readValue(uploadData,
                                    new TypeReference<List<Etikett>>() {
                                    }));
                        } else if ("Json-Context".equals(format)) {
                            Globals.profile.addJsonContextData(
                                    (Map<String, Object>) new ObjectMapper().readValue(uploadData, Map.class));
                        }
                        flash("info", "File uploaded");
                        return redirect(routes.Application.contextAsHtml());
                    }
                } else {
                    flash("error", "Missing file");
                    return redirect(routes.Application.contextAsHtml());
                }
            } catch (Exception e) {
                play.Logger.warn("", e);
                return redirect(routes.Application.contextAsHtml());
            }
        });
    }

    /**
     * @return a simple upload form for rdf files
     */
    public static Result upload() {
        return ok(upload.render());
    }

    /**
     * @return a jsonLd Context
     */
    public static Promise<Result> asJsonLdContext() {
        return Promise.promise(() -> {
            try {
                Map<String, Object> contextObject = Globals.profile.getContext();
                return ok(json(contextObject));
            } catch (Exception e) {
                play.Logger.warn("", e);
                return redirect(routes.Application.contextAsHtml());
            }
        });
    }

    public static Result deleteScreen(String url) {
        if (url != null) {
            Etikett e = Globals.profile.findEtikett(url);
            Form<Etikett> form = Form.form(Etikett.class).fill(e);
            return ok(delete.render(form, url));
        }
        return ok(delete.render(Form.form(Etikett.class), url));
    }

    /**
     * @return a simple form for new entries
     */
    public static Result edit(String url) {
        if (url != null) {
            Etikett e = Globals.profile.getValue(url);
            play.Logger.info("Edit " + e);
            Form<Etikett> form = Form.form(Etikett.class).fill(e);
            return ok(edit.render(form));
        }
        return ok(edit.render(Form.form(Etikett.class)));
    }

    /**
     * @return a simple form for new entries
     */
    public static Result update() {
        Form<Etikett> form = Form.form(Etikett.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(edit.render(form));
        } else {
            Etikett u = form.get();
            Globals.profile.addJsonData(u);
            return redirect(routes.Application.contextAsHtml());
        }
    }

    /**
     * @return a simple form for new entries
     */
    public static Promise<Result> delete(String url) {
        play.Logger.info("Try to delete " + url);
        if (url != null) {
            Etikett e = Globals.profile.getValue(url);

            if (e == null) {
                flash("info", "Delete not possible. Resource does not exist.");
                return getColumn(null, null, null);
            }
            e.delete();
            return getColumn(null, null, null);
        }
        flash("error", "Missing Parameter url");
        return getColumn(null, null, null);
    }

    public static Promise<Result> asRawJsonLdContext() {
        return Promise.promise(() -> {
            try {
                Map<String, Object> contextObject = Globals.profile.getRawContext();
                return ok(json(contextObject));
            } catch (Exception e) {
                play.Logger.warn("", e);
                return redirect(routes.Application.getColumn(null, null, null));
            }
        });
    }

    public static Promise<Result> asContextAnnotations() {
        return Promise.promise(() -> {
            try {
                Map<String, Object> contextObject = Globals.profile.getContextAnnotation();
                return ok(json(contextObject));
            } catch (Exception e) {
                play.Logger.warn("", e);
                return redirect(routes.Application.getColumn(null, null, null));
            }
        });
    }

    public static Promise<Result> deleteCache() {
        return Promise.promise(() -> {
            try {
                return ok(json(Globals.profile.deleteCacheValues()));
            } catch (Exception e) {
                play.Logger.warn("", e);
                return redirect(routes.Application.getColumn(null, null, null));
            }
        });
    }

    public static Promise<Result> editWeights() {
        return Promise.promise(() -> {
            try {
                response().setHeader("Content-Type", "text/html; charset=utf-8");
                return ok(editWeights.render("Edit Weights", getContextEntriesSortedByWeight()));
            } catch (Exception e) {
                play.Logger.debug("", e);
                return status(500, message.render("Server encounters internal problem: " + e.getMessage()));
            }
        });
    }

    public static Promise<Result> updateWeights() {
        return Promise.promise(() -> {
            List<String> uris = Arrays.asList(request().body().asFormUrlEncoded().get("uri"));
            List<String> weights = Arrays.asList(request().body().asFormUrlEncoded().get("weight"));
            Collection<Etikett> uploadData = new ArrayList<>();
            for (int i = 0; i < uris.size(); i++) {
                String uri = uris.get(i);
                String weight = weights.get(i);
                Etikett e = Globals.profile.getValue(uri);
                e.setWeight(weight);
                uploadData.add(e);
            }
            Globals.profile.addJsonData(uploadData);
            return redirect(routes.Application.contextAsHtml());

        });
    }

    private static List<Etikett> getContextEntriesSortedByWeight() {
        List<Etikett> result = new ArrayList<Etikett>(Globals.profile.getContextValues());
        result.sort((a, b) -> Integer.parseInt(a.getWeight()) - Integer.parseInt(b.getWeight()));
        return result;
    }
}
