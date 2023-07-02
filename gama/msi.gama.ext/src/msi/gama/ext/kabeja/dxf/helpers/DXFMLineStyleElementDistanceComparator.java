/*
   Copyright 2008 Simon Mieth

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package msi.gama.ext.kabeja.dxf.helpers;

import java.util.Comparator;

import msi.gama.ext.kabeja.dxf.objects.DXFMLineStyleElement;


public class DXFMLineStyleElementDistanceComparator implements Comparator {
    public int compare(Object arg0, Object arg1) {
        DXFMLineStyleElement el1 = (DXFMLineStyleElement) arg0;
        DXFMLineStyleElement el2 = (DXFMLineStyleElement) arg1;

        if (el1.getOffset() > el2.getOffset()) {
            return 1;
        } else if (el1.getOffset() < el2.getOffset()) {
            return -1;
        }

        return 0;
    }
}
