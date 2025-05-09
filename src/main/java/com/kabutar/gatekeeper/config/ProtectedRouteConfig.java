package com.kabutar.gatekeeper.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "config.protected-routes")
public class ProtectedRouteConfig {
	private boolean enabled;
	private List<Route> routes;
	
	
	public boolean isEnabled() {
		return enabled;
	}



	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}



	public List<Route> getRoutes() {
		return routes;
	}



	public void setRoutes(List<Route> routes) {
		this.routes = routes;
	}



	@Override
	public String toString() {
		return "ProtectedRouteConfig [enabled=" + enabled + ", routes=" + routes + "]";
	}





	public static class Route {
        private String id;
        private String path;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

		@Override
		public String toString() {
			return "Route [id=" + id + ", path=" + path + "]";
		}
        
        
    }
}
