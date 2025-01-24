export function getValue(parentKey: string, childKey: string): any {
    const json = JSON.parse(process.env.LOCALE_JSON);

    return findValue(json, parentKey, childKey);
}

function findValue(obj: any, searchParentKey: string, searchChildKey: string): any {
    if (obj === null || typeof obj !== 'object') {
        return null;
    }

    if (obj.hasOwnProperty(searchParentKey)) {
        const parentObj = obj[searchParentKey];
        if (parentObj && typeof parentObj === 'object' && parentObj.hasOwnProperty(searchChildKey)) {
            return parentObj[searchChildKey];
        }
    }

    for (const value of Object.values(obj)) {
        const found = findValue(value, searchParentKey, searchChildKey);
        if (found !== null) {
            return found;
        }
    }

    return null;
}
