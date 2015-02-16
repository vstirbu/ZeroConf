/*
 * Based on 'Bonjour DNS-SD plugin for Cordova.' by jarnoh@komplex.org - March 2012
 */

#import "ZeroConf.h"
#import <sys/socket.h>
#import <netinet/in.h>
#import <arpa/inet.h>

@implementation ZeroConf

@synthesize browseCallback;
@synthesize netServiceBrowser;
@synthesize currentResolve;

#if !defined(__has_feature) || !__has_feature(objc_arc)
#error This plugin requires ARC
#endif

- (void)watch:(CDVInvokedUrlCommand*)command
{
  [self.netServiceBrowser stop];
  self.netServiceBrowser=nil;
  
  NSString *arg0 = [command.arguments objectAtIndex:0];
  NSArray *arr = [arg0 componentsSeparatedByString:@"."];
  NSString *regType = [NSString stringWithFormat:@"%@.%@", [arr objectAtIndex:0], [arr objectAtIndex:1] ];
  NSString *domain = [NSString stringWithFormat:@"%@.", [arr objectAtIndex:2]];
  NSLog(@"regType: %@, domain: %@", regType, domain);

  self.browseCallback = command.callbackId;
  self.netServiceBrowser = [[NSNetServiceBrowser alloc] init];
  self.netServiceBrowser.delegate = self;
  [self.netServiceBrowser searchForServicesOfType:regType inDomain:domain];
}

- (void)netServiceBrowser:(NSNetServiceBrowser*)netServiceBrowser didFindService:(NSNetService*)service moreComing:(BOOL)moreComing {
  NSLog(@"didFindService name %@", service.name);

  NSMutableDictionary* resultDict = [[NSMutableDictionary alloc] init];
  [resultDict setObject:[NSNumber numberWithBool:TRUE] forKey:@"serviceFound"];
  [resultDict setObject:service.name forKey:@"serviceName"];
  [resultDict setObject:service.type forKey:@"regType"];
  [resultDict setObject:service.domain forKey:@"domain"];
  [resultDict setObject:[NSNumber numberWithBool:moreComing] forKey:@"moreComing"];

  self.currentResolve = [[NSNetService alloc] initWithDomain:service.domain type:service.type name:service.name];
  [self.currentResolve setDelegate:self];
  [self.currentResolve resolveWithTimeout:0.0];
}

- (void)netServiceDidResolveAddress:(NSNetService *)service {
  NSLog(@"netServiceDidResolveAddress: %@",service);

  NSMutableDictionary* resultDict = [[NSMutableDictionary alloc] init];
  NSMutableArray *addresses = [[NSMutableArray alloc] init]; //arrayWithObjects:

  for (NSData* data in [service addresses]) {
      struct sockaddr_in* socketAddress = (struct sockaddr_in*) [data bytes];
      int sockFamily = socketAddress->sin_family;
    
      char buf[100];
      const char* addressStr = inet_ntop(sockFamily, &(socketAddress->sin_addr), buf, sizeof(buf));
      NSString *address = [NSString stringWithUTF8String:addressStr];
    
      [addresses addObject:address];
  }

  [resultDict setObject:addresses forKey:@"addresses"];
  [resultDict setObject:[NSNumber numberWithBool:TRUE] forKey:@"serviceResolved"];
  [resultDict setObject:service.hostName forKey:@"server"];
  [resultDict setObject:service.hostName forKey:@"hostName"];
  [resultDict setObject:[NSNumber numberWithInteger:service.port] forKey:@"port"];
  [resultDict setObject:service.name forKey:@"serviceName"];

  NSString *type = [NSString stringWithFormat:@"%@%@", service.type, service.domain];
  [resultDict setObject:type forKey:@"type"];

  NSString *domain = [service.domain substringToIndex:service.domain.length-1];
  [resultDict setObject:domain forKey:@"domain"];
  [resultDict setObject:service.name forKey:@"name"];

  NSDictionary *jsonObj = [ [NSDictionary alloc]
                         initWithObjectsAndKeys :
                         resultDict, @"service",
                         @"added",@"action",
                         nil];

  CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsDictionary: jsonObj ];
  [result setKeepCallbackAsBool:YES];
  [self.commandDelegate sendPluginResult:result callbackId:self.browseCallback];
}

@end
