all:

push-apk:
	scp app/build/outputs/apk/app-debug.apk \
		k.pacew.org:/var/www/html/blank/app-$(USER).apk

wapp:
	gradlew assembleDebug

load-wapp:
	adb install -r app/build/outputs/apk/app-debug.apk
