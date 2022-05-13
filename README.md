[![Release](https://jitpack.io/v/hannesa2/LiveEdgeDetection.svg)](https://jitpack.io/#hannesa2/LiveEdgeDetection)

# LiveEdgeDetection

LiveEdgeDetection is an Android document detection library built on top of OpenCV. It scans documents from camera live mode and allows you to adjust crop using the detected 4 edges and performs perspective transformation of the cropped image.

**It works best with a dark background.**

## OpenCV

The source code of OpenCV is here https://git.mxtracks.info/opencv/openCV-android-sdk
The code was too big for github, but the main problem was jitpack.io was not able to build
https://github.com/jitpack/jitpack.io/issues/4119

## Screenshots

![Use darker bg](https://github.com/hannesa2/LiveEdgeDetection/blob/master/use_darker_bg.png)
![Move closer](https://github.com/hannesa2/LiveEdgeDetection/blob/master/move_closer.png)
![Move away](https://github.com/hannesa2/LiveEdgeDetection/blob/master/move_away.png)
![Adjust angle](https://github.com/hannesa2/LiveEdgeDetection/blob/master/adjust_angle.png)
![Hold still](https://github.com/hannesa2/LiveEdgeDetection/blob/master/hold_still.png)
![Adjust crop](https://github.com/hannesa2/LiveEdgeDetection/blob/master/adjust_crop.png)

## Integrating into your project
This library is available in [JitPack.io](https://jitpack.io/#hannesa2/LiveEdgeDetection/) repository.
To use it, make sure to add the below inside root build.gradle file

```
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" } // for this library
        maven { url 'https://artifactory.mxtracks.info/artifactory/gradle-dev-local/' } // for the OpenCV library
    }
}
```

and add the repository's url to the app's build.gradle file.

```
dependencies {
   implementation 'com.github.hannesa2:LiveEdgeDetection:$latestVersion'
}
```
## Usage
Out of the box it uses OpenCV.

1. Start **startActivityForResult** from your activity
```
startActivityForResult(Intent(this, ScanActivity::class.java), REQUEST_CODE)
```
2. Get a file path for cropped image on **onActivityResult**
```
val filePath = data?.extras?.getString(ScanConstants.SCANNED_RESULT)
val baseBitMap = filePath.decodeBitmapFromFile()
```
3. Display the image using **TouchImageView** https://github.com/MikeOrtiz/TouchImageView
```
<com.ortiz.touchview.TouchImageView
        android:id="@+id/scanned_image"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="center" />
```

## License LiveEdgeDetection
```
Copyright 2022 hannesa2.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## License PDF
```
https://github.com/itext/itext7/blob/develop/LICENSE.md

This program is offered under a commercial and under the AGPL license.
For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

AGPL licensing:
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```
