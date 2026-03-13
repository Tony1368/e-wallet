/**
 * Service to collect client device and location information
 */
class ClientInfoService {
  
  /**
   * Get client IP address (this will be detected by the server)
   */
  static getClientIpAddress() {
    // IP address will be detected by the server from the request
    return null;
  }

  /**
   * Get user agent string
   */
  static getUserAgent() {
    return navigator.userAgent;
  }

  /**
   * Get device type
   */
  static getDeviceType() {
    const userAgent = navigator.userAgent.toLowerCase();
    
    if (/mobile|android|iphone|ipad|phone/i.test(userAgent)) {
      return 'Mobile';
    }
    if (/tablet|ipad/i.test(userAgent)) {
      return 'Tablet';
    }
    return 'Desktop';
  }

  /**
   * Get browser information
   */
  static getBrowser() {
    const userAgent = navigator.userAgent;
    
    if (userAgent.includes('Chrome')) {
      return 'Chrome';
    }
    if (userAgent.includes('Firefox')) {
      return 'Firefox';
    }
    if (userAgent.includes('Safari')) {
      return 'Safari';
    }
    if (userAgent.includes('Edge')) {
      return 'Edge';
    }
    if (userAgent.includes('Opera')) {
      return 'Opera';
    }
    return 'Unknown';
  }

  /**
   * Get operating system
   */
  static getOperatingSystem() {
    const userAgent = navigator.userAgent;
    
    if (userAgent.includes('Windows')) {
      return 'Windows';
    }
    if (userAgent.includes('Mac')) {
      return 'macOS';
    }
    if (userAgent.includes('Linux')) {
      return 'Linux';
    }
    if (userAgent.includes('Android')) {
      return 'Android';
    }
    if (userAgent.includes('iOS')) {
      return 'iOS';
    }
    return 'Unknown';
  }

  /**
   * Get timezone
   */
  static getTimezone() {
    return Intl.DateTimeFormat().resolvedOptions().timeZone;
  }

  /**
   * Get location information using Geolocation API
   */
  static async getLocationInfo() {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        resolve({
          latitude: null,
          longitude: null,
          country: null,
          city: null,
          region: null
        });
        return;
      }

      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const { latitude, longitude } = position.coords;
          
          try {
            // Try to get location details from coordinates
            const response = await fetch(
              `https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${latitude}&longitude=${longitude}&localityLanguage=en`
            );
            
            if (response.ok) {
              const data = await response.json();
              resolve({
                latitude: latitude.toString(),
                longitude: longitude.toString(),
                country: data.countryName || null,
                city: data.city || null,
                region: data.principalSubdivision || null
              });
            } else {
              resolve({
                latitude: latitude.toString(),
                longitude: longitude.toString(),
                country: null,
                city: null,
                region: null
              });
            }
          } catch (error) {
            // If reverse geocoding fails, return coordinates only
            resolve({
              latitude: latitude.toString(),
              longitude: longitude.toString(),
              country: null,
              city: null,
              region: null
            });
          }
        },
        (error) => {
          // If geolocation fails, return null values
          resolve({
            latitude: null,
            longitude: null,
            country: null,
            city: null,
            region: null
          });
        },
        {
          enableHighAccuracy: false,
          timeout: 5000,
          maximumAge: 300000 // 5 minutes
        }
      );
    });
  }

  /**
   * Get all client information
   */
  static async getAllClientInfo() {
    const locationInfo = await ClientInfoService.getLocationInfo();
    
    return {
      ipAddress: ClientInfoService.getClientIpAddress(),
      userAgent: ClientInfoService.getUserAgent(),
      deviceType: ClientInfoService.getDeviceType(),
      browser: ClientInfoService.getBrowser(),
      operatingSystem: ClientInfoService.getOperatingSystem(),
      timezone: ClientInfoService.getTimezone(),
      ...locationInfo
    };
  }
}

export default ClientInfoService; 