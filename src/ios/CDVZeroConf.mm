/**
 * ZeroConf plugin for Cordova/Phonegap
 *
 * Copyright (c) 2016 Jonas Schnelli <dev@jonasschnelli.ch>
 * MIT License
 *
 * @author Jonas Schnelli
 * Available under the terms of the MIT License.
 *
 * TODO currently, only the watch command is supported.
 * TODO the iOS implementation should support the same
 * TODO features as the android version.
 *
 */

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <Foundation/NSNetServices.h>

#import <Cordova/CDVPlugin.h>
#import <arpa/inet.h>

@interface CDVZeroConf : CDVPlugin <NSNetServiceBrowserDelegate, NSNetServiceDelegate> {
    NSNetServiceBrowser* _netServiceBrowser;
}
@property NSNetServiceBrowser* netServiceBrowser;
@property NSString* watchCommandCallbackId;
@property (nonatomic, strong) NSMutableArray *services;
- (void)watch:(CDVInvokedUrlCommand*)command;
@end

@implementation CDVZeroConf

+ (NSString *) stringFromAddress: (const struct sockaddr *) address
{
    if (address && address->sa_family == AF_INET)
    {
        const struct sockaddr_in* sin = (struct sockaddr_in *) address;
        return [NSString stringWithFormat:@"%@", [NSString stringWithUTF8String:inet_ntoa(sin->sin_addr)]];
    }
    
    return nil;
}

- (void)watch:(CDVInvokedUrlCommand*)command
{
    self.services = [[NSMutableArray alloc] init];
    [self.netServiceBrowser stop];
    self.netServiceBrowser = [[NSNetServiceBrowser alloc] init];
    self.netServiceBrowser.delegate = self;
    [self.netServiceBrowser searchForServicesOfType:command.arguments[0] inDomain:command.arguments[1]];

    //store the callback id
    self.watchCommandCallbackId = command.callbackId;
}

- (void)netServiceBrowser:(NSNetServiceBrowser*)netServiceBrowser didFindService:(NSNetService*)service moreComing:(BOOL)moreComing {

    if (!self.services) {
        self.services = [[NSMutableArray alloc] init];
    }
    [self.services addObject:service];
    [service setDelegate:self];
    [service resolveWithTimeout:5.0];
}

// Sent when addresses are resolved
- (void)netServiceDidResolveAddress:(NSNetService *)service
{
    NSMutableArray *addrs = [NSMutableArray array];
    NSLog(@"%@, %@, %@, %ld", service.name, service.domain, service.addresses, service.port);
    for (NSData *data in service.addresses)
    {
        struct sockaddr *adr = (struct sockaddr*)data.bytes;
        NSString *ipStr = [CDVZeroConf stringFromAddress:adr];
        if (ipStr)
            [addrs addObject:ipStr];
    }
    NSDictionary *serviceJson = @{
                                  @"domain" : service.domain,
                                  @"name" : service.name,
                                  @"type" : service.type,
                                  @"port" : [NSNumber numberWithInteger:service.port],
                                  @"addresses" : addrs};
    NSDictionary *jsonObj = @{@"action" : @"Added", @"service" : serviceJson};

    CDVPluginResult *pluginResult = [ CDVPluginResult
                                     resultWithStatus    : CDVCommandStatus_OK
                                     messageAsDictionary : jsonObj
                                     ];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.watchCommandCallbackId];
}

@end
