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
package helper;

import java.io.InputStream;

import org.openrdf.model.Graph;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
public class RdfUtils {

    /**
     * @param inputStream
     *            an Input stream containing rdf data
     * @param inf
     *            the rdf format
     * @param baseUrl
     *            see sesame docu
     * @return a Graph representing the rdf in the input stream
     */
    public static Graph readRdfToGraph(InputStream inputStream, RDFFormat inf,
	    String baseUrl) {
	try {
	    RDFParser rdfParser = Rio.createParser(inf);
	    org.openrdf.model.Graph myGraph = new TreeModel();
	    StatementCollector collector = new StatementCollector(myGraph);
	    rdfParser.setRDFHandler(collector);
	    rdfParser.parse(inputStream, baseUrl);
	    return myGraph;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }
}
