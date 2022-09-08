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
package com.wavemaker.runtime.data.filter.parser.utils.models;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Model {
    private Byte wmByte;
    private Short wmShort;
    private Integer wmInteger;
    private Long wmLong;
    private BigInteger wmBigInteger;
    private Float wmFloat;
    private Double wmDouble;
    private BigDecimal wmBigDecimal;
    private Boolean wmBoolean;
    private Character wmCharacter;
    private String wmString;
    private Date wmDate;
    private Time wmTime;
    private LocalDateTime wmLocalDateTime;
    private Timestamp wmTimestamp;
    private ModelChild child;
}