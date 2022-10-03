/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wavemaker.runtime.data.export.hqlquery;

import org.hibernate.ScrollableResults;

import com.wavemaker.runtime.data.export.QueryExtractor;

/**
 * @author <a href="mailto:anusha.dharmasagar@wavemaker.com">Anusha Dharmasagar</a>
 * @since 22/5/17
 */
public class HqlQueryExtractor implements QueryExtractor {

    private ScrollableResults results;
    private int currentIndex;

    public HqlQueryExtractor(final ScrollableResults results) {
        this.results = results;
    }

    @Override
    public boolean next() throws Exception {
        final boolean hasNext = results.next();
        this.currentIndex++;
        return hasNext;
    }

    @Override
    public boolean isFirstRow() {
        //since isFirst() or getRow() methods in org.hibernate.ScrollableResults are not supported in few DBs.
        return currentIndex == 1;
    }

    @Override
    public Object getCurrentRow() throws Exception {
        return results.get();
    }
}
