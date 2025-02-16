export interface Group {
    id: string;
    name: string;
    path?: string;
    subGroups: Group[];
    parentGroupId?: string;
}
