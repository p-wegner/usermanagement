export interface User {
  id: string;
  username: string;
  fullName: string;
  email: string;
  language?: string;
}

export interface UserCollection {
  id: string;
  name: string;
  description?: string;
  members: User[];
}
