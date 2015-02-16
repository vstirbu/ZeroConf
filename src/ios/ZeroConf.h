/*
 * Based on 'Bonjour DNS-SD plugin for Cordova.' by jarnoh@komplex.org - March 2012
 */

#import <Foundation/Foundation.h>
#import <Foundation/NSNetServices.h>

#import <Cordova/CDVPlugin.h>

@interface ZeroConf : CDVPlugin <NSNetServiceBrowserDelegate, NSNetServiceDelegate>
{
  NSString* browseCallback;
  NSNetServiceBrowser* netServiceBrowser;
  NSNetService* currentResolve;
}

@property (nonatomic, copy) NSString* browseCallback;
@property (nonatomic, retain, readwrite) NSNetServiceBrowser* netServiceBrowser;
@property (nonatomic, retain, readwrite) NSNetService* currentResolve;

- (void)watch:(CDVInvokedUrlCommand*)command;

@end
