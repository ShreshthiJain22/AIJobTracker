import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Application } from '../models/application';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private baseUrl = 'https://aijobtracker.duckdns.org/api/applications';

  constructor(private http: HttpClient) {}

  getAllApplications(): Observable<Application[]> {
    return this.http.get<Application[]>(this.baseUrl);
  }

  getApplicationById(id: number): Observable<Application> {
    return this.http.get<Application>(`${this.baseUrl}/${id}`);
  }

  createApplication(application: Application): Observable<Application> {
    return this.http.post<Application>(this.baseUrl, application);
  }

  updateApplication(id: number, application: Application): Observable<Application> {
    return this.http.put<Application>(`${this.baseUrl}/${id}`, application);
  }

  deleteApplication(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
  getQuoteOfTheDay(): Observable<string> {
    return this.http.get(`${this.baseUrl}/quote-of-the-day`, { responseType: 'text' });
  }
}