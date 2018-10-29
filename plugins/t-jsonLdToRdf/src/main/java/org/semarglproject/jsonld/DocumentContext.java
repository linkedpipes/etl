/**
 * Content of this file was changed for need of LinkedPipes ETL.
 * <p>
 * Copyright 2012-2013 the Semargl contributors. See AUTHORS for more details.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.semarglproject.jsonld;

import org.semarglproject.vocab.RDF;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds document context: source IRI and bnode generation info.
 */
final class DocumentContext {

    String iri;

    private Map<String, String> bnodeMapping = new HashMap<String, String>();

    private int nextBnodeId;

    DocumentContext() {
        nextBnodeId = 0;
    }

    String resolveBNode(String value) {
        if (value.startsWith(RDF.BNODE_PREFIX) || value.startsWith('[' + RDF.BNODE_PREFIX)
                && value.charAt(value.length() - 1) == ']') {
            String name;
            if (value.charAt(0) == '[') {
                name = value.substring(RDF.BNODE_PREFIX.length() + 1, value.length() - 1);
            } else {
                name = value.substring(RDF.BNODE_PREFIX.length());
            }
            if (!bnodeMapping.containsKey(name)) {
                bnodeMapping.put(name, createBnode(false));
            }
            return bnodeMapping.get(name);
        }
        return null;
    }

    String createBnode(boolean shortenable) {
        if (shortenable) {
            return RDF.BNODE_PREFIX + 'n' + (nextBnodeId++) + RDF.SHORTENABLE_BNODE_SUFFIX;
        }
        return RDF.BNODE_PREFIX + 'n' + nextBnodeId++;
    }

    void clear() {
        bnodeMapping.clear();
        iri = null;
    }

}
