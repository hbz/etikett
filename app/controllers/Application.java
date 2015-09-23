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

import helper.BasicAuth;
import helper.Globals;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Etikett;
import play.data.Form;
import play.libs.F.Promise;
import play.mvc.Call;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import views.html.*;

/**
 * @author Jan Schnasse
 *
 */
public class Application extends MyController {

    /**
     * @param urlAddress
     * @return the data of the corresponding row
     */
    public static Result row(String urlAddress) {

	if (request().accepts("text/html")) {
	    return asHtml(urlAddress);
	} else {
	    return asJson(urlAddress);
	}

    }

    /**
     * @param urlAddress
     *            a url address
     * @param column
     *            the name of the column
     * @return a string with the content of the column
     */
    public static Promise<Result> getColumn(String urlAddress, String column) {
	return Promise.promise(() -> {
	    if (column == null) {
		return row(urlAddress);
	    }
	    response().setHeader("Content-Type", "text/plain; charset=utf-8");
	    if (column != null && !column.isEmpty() && urlAddress != null) {
		Etikett entry = Globals.profile.getValue(urlAddress);
		switch (column) {
		case "Icon":
		    return ok(entry.icon);
		case "Label":
		    return ok(entry.label);
		case "Name":
		    return ok(entry.name);
		case "Uri":
		    return ok(entry.uri);
		case "RefType":
		    return ok(entry.referenceType);
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
	    response().setHeader("Content-Type",
		    "application/json; charset=utf-8");
	    if (urlAddress != null) {
		Etikett entry = Globals.profile.getValue(urlAddress);
		ArrayList<Etikett> result = new ArrayList<Etikett>();
		result.add(entry);
		return ok(json(result));
	    } else {
		return ok(json(new ArrayList<Etikett>(
			Globals.profile.getValues())));
	    }
	} catch (Exception e) {
	    play.Logger.debug("", e);
	    return status(500, json(e.toString()));
	}

    }

    private static Result asHtml(String urlAddress) {
	try {
	    response().setHeader("Content-Type", "text/html; charset=utf-8");
	    if (urlAddress != null) {
		Etikett entry = Globals.profile.getValue(urlAddress);
		ArrayList<Etikett> result = new ArrayList<Etikett>();
		result.add(entry);
		return ok(index.render(result));
	    } else {
		return ok(index.render(new ArrayList<Etikett>(Globals.profile
			.getValues())));
	    }
	} catch (Exception e) {
	    play.Logger.debug("", e);
	    return status(
		    500,
		    message.render("Server encounters internal problem: "
			    + e.getMessage()));
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
    public static String EncodeURL(final String url)
	    throws java.io.UnsupportedEncodingException {
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
    public static String EncodeURL(Call call)
	    throws java.io.UnsupportedEncodingException {
	return EncodeURL(call.toString());
    }

    /**
     * @return http status
     */
    @BasicAuth
    public static Promise<Result> addSkosData() {
	return new ModifyAction()
		.call(() -> {
		    try {
			MultipartFormData body = request().body()
				.asMultipartFormData();
			FilePart data = body.getFile("data");
			if (data != null) {
			    // String fileName = data.getFilename();
			    // String contentType = data.getContentType();
		File file = data.getFile();
		try (FileInputStream uploadData = new FileInputStream(file)) {
		    Globals.profile.addRdfData(uploadData);
		    flash("info", "File uploaded");
		    return redirect(routes.Application.getColumn(null, null));
		}
	    } else {
		flash("error", "Missing file");
		return redirect(routes.Application.getColumn(null, null));
	    }
	} catch (Exception e) {
	    return redirect(routes.Application.getColumn(null, null));
	}
    })  ;
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
	return Promise
		.promise(() -> {
		    try {
			List<Etikett> ls = new ArrayList<Etikett>(
				Globals.profile.getValues());
			Map<String, Object> pmap;
			Map<String, Object> cmap = new HashMap<String, Object>();
			for (Etikett l : ls) {
			    if ("class".equals(l.referenceType)
				    || l.referenceType == null
				    || l.name == null)
				continue;
			    pmap = new HashMap<String, Object>();
			    pmap.put("@id", l.uri);
			    pmap.put("label", l.label);
			    pmap.put("icon", l.icon);
			    if ("@id".equals(l.referenceType)) {
				pmap.put("@type", l.referenceType);
			    }
			    cmap.put(l.name, pmap);
			}
			Map<String, Object> contextObject = new HashMap<String, Object>();
			contextObject.put("@context", cmap);
			return ok(json(contextObject));
		    } catch (Exception e) {
			play.Logger.warn("", e);
			return redirect(routes.Application
				.getColumn(null, null));
		    }
		});
    }

    /**
     * @param url
     * @return a simple form for new entries
     */
    public static Result edit(String url) {
	if (url != null) {
	    Etikett e = Globals.profile.getValue(url);
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
	    u.update();
	}
	return redirect(routes.Application.getColumn(null, null));
    }

    /**
     * @param url
     * @return a simple form for new entries
     */
    public static Result delete(String url) {
	play.Logger.debug("Try to delete " + url);
	if (url != null) {
	    Etikett e = Globals.profile.getValue(url);
	    e.delete();
	    redirect(routes.Application.getColumn(null, null));
	}
	flash("error", "Missing Parameter url");
	return redirect(routes.Application.getColumn(null, null));
    }
}
