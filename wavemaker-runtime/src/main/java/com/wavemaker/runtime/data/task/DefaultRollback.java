/*
 *  Copyright (C) 2012-2013 CloudJee, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.runtime.data.task;

/**
 * Marker interface.
 * 
 * A Task implementing this interface will run in a tx that will be rolled back after the task has exited from its run
 * method - iff the tx was started for the Task instance. If a tx is already in progress when the task starts running,
 * the rollback will not happen.
 * 
 * @author Simon Toens
 */
public interface DefaultRollback {
}
