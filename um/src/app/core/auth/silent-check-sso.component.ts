import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';

@Component({
  template: ''
})
export class SilentCheckSsoComponent implements OnInit {
  constructor(private keycloakService: KeycloakService) {}

  async ngOnInit() {
    try {
      await this.keycloakService.init();
    } catch (error) {
      console.error('Error during silent SSO check:', error);
    }
  }
}
