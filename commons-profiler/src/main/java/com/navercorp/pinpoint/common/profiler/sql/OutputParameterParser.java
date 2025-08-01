/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.profiler.sql;

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.navercorp.pinpoint.common.profiler.sql.ParserContext.lookAhead1;

/**
 * @author emeroad
 */
public class OutputParameterParser {

    public static final char SEPARATOR = Tokens.SEPARATOR;

    public List<String> parseOutputParameter(String outputParams) {
        // may also need to know about the parsing result 
        if (StringUtils.isEmpty(outputParams)) {
            return Collections.emptyList();
        }

        final List<String> result = new ArrayList<>();
        StringBuilder params = new StringBuilder();
        for (int index = 0; index < outputParams.length(); index++) {
            final char ch = outputParams.charAt(index);
            if (ch == SEPARATOR) {
                int ahead = lookAhead1(outputParams, index);
                if (ahead == SEPARATOR) {
                    params.append(SEPARATOR);
                    index++;
                } else {
                    if (ahead == ' ') {
                        index++;
                    }
                    result.add(params.toString());
                    params = new StringBuilder();
                }
            } else {
                params.append(ch);
            }
        }

        result.add(params.toString());

        return result;
    }


}
