/*
 *  Copyright (C) 2008-2009 WaveMaker Software, Inc.
 *
 *  This file is part of the WaveMaker Server Runtime.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.runtime.server;

import com.wavemaker.runtime.service.annotations.ExposeToClient;
import com.wavemaker.runtime.service.annotations.HideFromClient;

/**
 * @author Matt Small
 */
public class HideExposeClasses {

    public static class Default {

        public void foo() {
        }

        @HideFromClient
        public void hide() {
        }

        @ExposeToClient
        public void expose() {
        }
    }

    @ExposeToClient
    public static class Expose {

        public void foo() {
        }

        @HideFromClient
        public void hide() {
        }

        @ExposeToClient
        public void expose() {
        }
    }

    @HideFromClient
    public static class Hide {

        public void foo() {
        }

        @HideFromClient
        public void hide() {
        }

        @ExposeToClient
        public void expose() {
        }
    }

    public static class Conflict {

        @HideFromClient
        @ExposeToClient
        public void conflict() {
        }
    }

    @HideFromClient
    public static class DefaultConflict {

        @HideFromClient
        @ExposeToClient
        public void conflict() {
        }
    }
}