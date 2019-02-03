/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.naman14.timberx.logging

import com.crashlytics.android.Crashlytics
import timber.log.Timber

/** @author Aidan Follestad (@afollestad) */
class FabricTree : Timber.Tree() {

  override fun log(
    priority: Int,
    tag: String?,
    message: String,
    t: Throwable?
  ) {
    if (t != null) {
      Crashlytics.setString("crash_tag", tag)
      Crashlytics.logException(t)
    } else {
      Crashlytics.log(priority, tag, message)
    }
  }
}
