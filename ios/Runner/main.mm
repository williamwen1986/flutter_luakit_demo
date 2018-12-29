#import <Flutter/Flutter.h>
#import <UIKit/UIKit.h>
#import <LuakitPod/oc_helpers.h>
#import "AppDelegate.h"

int main(int argc, char* argv[]) {
    startLuakit(argc, argv);
  @autoreleasepool {
    return UIApplicationMain(argc, argv, nil, NSStringFromClass([AppDelegate class]));
  }
}
