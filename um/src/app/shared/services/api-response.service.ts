import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ApiResponseService {
  
  handleResponse<T, R>(
    response$: Observable<ApiResponse<T>>, 
    mapper: (data: T) => R,
    errorMessage: string = 'Operation failed'
  ): Observable<R> {
    return response$.pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error(response.error || errorMessage);
        }
        return mapper(response.data);
      })
    );
  }

  handleListResponse<T, R>(
    response$: Observable<ApiResponse<T[]>>,
    mapper: (data: T) => R,
    errorMessage: string = 'Operation failed'
  ): Observable<R[]> {
    return response$.pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error(response.error || errorMessage);
        }
        return response.data.map(mapper);
      })
    );
  }
}
