/*
 * Copyright (C) 2011 Jason Fry
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Jason Fry - jasonfry.co.uk
 * @version 1.0
 * 
 */

package uk.co.jasonfry.android.tools.util;

import android.view.View;

public class OverScrollDisabler
{
	public static void disableOverScroll(View view)
	{
		view.setOverScrollMode(View.OVER_SCROLL_NEVER);
	}
}