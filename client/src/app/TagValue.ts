export class TagValue {

    key: string;

    values :string[] = [];

    constructor(key,values) {
        this.key = key;
        this.values = values;
    }
}