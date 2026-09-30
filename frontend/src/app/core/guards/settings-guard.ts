import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { UserSettingsService } from '../services/user-settings';

export const settingsGuard: CanActivateFn = (route, state) => {
  const userSettingsService = inject(UserSettingsService);
  const router = inject(Router);

  return userSettingsService.getMySettings().pipe(
    map((settings) => {
      if (settings && settings.goals && settings.goals.length >= 2) {
        return true;
      }
      router.navigate(['/columns-setup']);
      return false;
    }),
    catchError((error) => {
      if (error.status === 401 || error.status === 403) {
        return of(false);
      }
      router.navigate(['/columns-setup']);
      return of(false);
    })
  );
};