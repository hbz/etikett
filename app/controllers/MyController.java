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

import java.io.StringWriter;

import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Jan Schnasse
 *
 */
public class MyController extends Controller {
    protected static ObjectMapper mapper = new ObjectMapper();

    private static void setJsonHeader() {
	response().setHeader("Access-Control-Allow-Origin", "*");
	response().setContentType("application/json");
    }

    /**
     * @param obj
     *            an arbitrary object
     * @return json serialization of obj
     */
    public static Result getJsonResult(Object obj) {
	setJsonHeader();
	try {
	    return ok(json(obj));
	} catch (Exception e) {
	    return internalServerError("Not able to create response!");
	}
    }

    protected static String json(Object obj) {
	try {
	    StringWriter w = new StringWriter();
	    mapper.writeValue(w, obj);
	    String result = w.toString();
	    return result;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }
}
