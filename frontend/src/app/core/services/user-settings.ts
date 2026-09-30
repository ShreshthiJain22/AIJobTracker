import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserSettings } from '../models/user-settings';

@Injectable({
  providedIn: 'root'
})
export class UserSettingsService {
  private baseUrl = 'https://aijobtracker.duckdns.org/api/settings';

  constructor(private http: HttpClient) {}

  getMySettings(): Observable<UserSettings> {
    return this.http.get<UserSettings>(`${this.baseUrl}/me`);
  }

  saveMySettings(settings: UserSettings): Observable<UserSettings> {
    return this.http.post<UserSettings>(`${this.baseUrl}/me`, settings);
  }
}