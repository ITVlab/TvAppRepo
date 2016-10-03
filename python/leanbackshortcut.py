from distutils.dir_util import copy_tree
from subprocess import call
from tempfile import mkstemp
import os
import shutil
import sys
import urllib
# Get all of the new apks from firebase
# Run through them all
# Replace certain things
# Upload to server

def write_keys():
    keys = open('keys.txt', 'r')
    replace('./temp_apk/app/build.gradle', 
            'storeFile file("myreleasekey.keystore")',
            keys.readline())
    replace('./temp_apk/app/build.gradle', 
            'storePassword "password"',
            keys.readline())
    replace('./temp_apk/app/build.gradle', 
            'keyAlias "MyReleaseKey"',
            keys.readline())
    replace('./temp_apk/app/build.gradle', 
            'keyPassword "password"',
            keys.readline())

def clone_shortcut():
    f = open('config.txt', 'r')
    print 'Reading from config.txt'
    for line in f:
        if "SHORTCUT_APK_DIRECTORY=" in line:
            uri = line[23:].strip()
            copy_tree(uri, './temp_apk')
            
def compile_app(packageName):
    # Change our local properties
    f = open('config.txt', 'r')
    print 'Reading from config.txt'
    for line in f:
        if "ANDROID_SDK_LOCATION=" in line:
            uri = line[21:].strip()
            localProperties = open('./temp_apk/local.properties', 'w')
            localProperties.write('sdk.dir=' + uri)
            os.environ['ANDROID_HOME'] = uri
            print 'Android SDK location set to ' + uri
    # Once everything is good, program from cmd line
    # call(['./temp_apk/gradlew'])
    print 'Building APK...'
    # Is it good? Let's assume so
    os.chdir('temp_apk/');
    call(['./gradlew', 'clean'])
    call(['./gradlew', 'assembleRelease', '--info', '--debug', '--stacktrace'])
    os.chdir('../');
    # file should be in ./temp_apk/app/app-release.apk
    upload_apk(packageName)
    
def upload_apk(packageName):
    # Move to another directory
    print 'APK created.'
    shutil.move('./temp_apk/app/app-release.apk', './leanback_shortcuts/' + packageName + '.apk')
    print 'Moved to leanback_shortcuts/' + packageName + '.apk'
    # Upload to Firebase
    print 'Apk uploaded successfully'
            
def generate_apk(appName, packageName, banner):
    clone_shortcut()
    # Edit app/build.gradle
    replace('./temp_apk/app/build.gradle', 
            'applicationId "news.androidtv.shortcut"',
            'applicationId "news.androidtv.shortcutgo.' + packageName + '"')
    # Edit Manifest
    replace('./temp_apk/app/src/main/AndroidManifest.xml', 
            'package="com.sample"',
            'package="news.androidtv.shortcutgo.' + packageName + '"')
    # Update MainActivity
    replace('./temp_apk/app/src/main/java/news/android/shortcut/MainActivity.java', 
            'import com.sample.R;',
            'import news.androidtv.shortcutgo.' + packageName + ';')
    
    write_keys()
    # Edit strings.xml
    strings = open('./temp_apk/app/src/main/res/values/strings.xml', 'w')
    strings.write('<resources>\
            <string name="app_name">' + appName + ' Shortcut</string>\
            <string name="activity_to_launch"></string>\
            <string name="package_name">' + packageName + '</string>\
        </resources>')
    # Download banner to ./temp_apk/app/src/main/res/drawable
    urllib.urlretrieve(banner, './temp_apk/app/src/main/res/drawable/tv_banner.png')    
    # Compile
    print 'Compiling app ' + packageName;
    compile_app(packageName)
            
# http://stackoverflow.com/questions/39086/search-and-replace-a-line-in-a-file-in-python
def replace(file_path, pattern, subst):
    #C reate temp file
    fh, abs_path = mkstemp()
    with open(abs_path,'w') as new_file:
        with open(file_path) as old_file:
            for line in old_file:
                new_file.write(line.replace(pattern, subst))

    os.close(fh)
    # Remove original file
    os.remove(file_path)
    # Move new file
    print "Temporarily copied file to " + abs_path
    print "Rewriting file at " + file_path
    shutil.move(abs_path, file_path)
    
# MAIN EXECUTION
if sys.argv[1] == '--debug' or sys.argv[1] == '-d':
    generate_apk("Cumulus TV", "com.felkertech.n.cumulustv", "https://github.com/Fleker/CumulusTV/blob/master/app/src/main/res/drawable-xhdpi/c_banner_3_2.jpg?raw=true")
else:
    # Run through Firebase
    print "Getting latest apps from Firebase"