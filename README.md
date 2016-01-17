# SearchWakayamToilet
Searching public toilets in wakayama by [toilet-map.csv](https://github.com/wakayama-pref-org/toilet-map/blob/master/toilet-map.csv).

## How to use
1. get Google Map API
2. create or add following contents on strings.xml
<pre>
                <resources>
                    <string name="app_name">SearchWakayamaToilet</string>
                    <string name="label_action"></string>
                    <string name="action_search">Search</string>
                    <string name="action_settings">Settings</string>
                    <string name="google_maps_key">Set google Maps Key</string>
                    <string name="toast_failed_getting_location">位置情報の取得に失敗しました\nもう一度ボタンを押してください</string>
                    <string name="toast_no_networks">機能を使用するためにはネットワークを有効にしてください</string>
                    <string name="request_enable_location">2</string>
                    <string name="handler_get_csv">0</string>
                    <string name="handler_get_location">1</string>
                    <string name="searchview_queryhint">名称や住所から検索</string>
                </resources>
</pre>
2. get toilet-map.csv and put it in app/src/main/assets

## Environment
* IDE: Android Studio 1.5.1
* Min SDK: 16

## Licence
[MIT](https://github.com/tcnksm/tool/blob/master/LICENCE)

### toilet-map
Project: https://github.com/wakayama-pref-org/toilet-map
License: CC BY 2.1 JP(https://github.com/wakayama-pref-org/toilet-map/blob/master/README.md)

### Lightweight-Stream-API
Project: https://github.com/aNNiMON/Lightweight-Stream-API
License: Apache License Version2.0(https://github.com/aNNiMON/Lightweight-Stream-API/blob/master/LICENSE)

### Gradle Retrolambda Plugin
Project: https://github.com/evant/gradle-retrolambda
License: Apache License Version2.0(https://github.com/evant/gradle-retrolambda/blob/master/LICENSE.txt)

## Author
[masanori840816](https://github.com/masanori840816)
